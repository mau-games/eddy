import time

import torch
import threading
import keyboard
import sys

from transformers import GPT2Tokenizer, GPTNeoForCausalLM, pipeline

# check feature-DesignerPersonas/src/main/python & java
# can use flask server to send data between java and python through localhost
# create state to interact with flask server

# ========== Vars ==========

shouldRun = True
prompt = "I saw an orc"
max_length = 100
teamName = "EleutherAI/"
modelName = "gpt-neo-1.3B"
model_save_directory = "./" + modelName
hasReceivedInput = False

# ========== Init ==========

# setting device on GPU if available, else CPU
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print('Using device:', device)
print()

# Attempt to load model from disk
# if Path(model_save_directory).exists() and exists(model_save_directory + "/pytorch_model.bin"):


print("Attempting to load model...", end=" ")
lStartTime = time.perf_counter()
model = GPTNeoForCausalLM.from_pretrained(model_save_directory)

# Model loaded successfully
if model:
    lEndTime = time.perf_counter()
    print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")
# Download and save model
else:
    print(f"Failed. Model not found, proceeding to download.\n")
    print("Downloading model...", end=" ")
    lStartTime = time.perf_counter()
    GPTNeoForCausalLM.from_pretrained(teamName + modelName).save_pretrained(model_save_directory)
    lEndTime = time.perf_counter()
    print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

# Attempt to load tokenizer from disk
print("Attempting to load tokenizer...", end=" ")
lStartTime = time.perf_counter()
tokenizer = GPT2Tokenizer.from_pretrained(model_save_directory, output_attentions=True)

# Tokenizer loaded successfully
if tokenizer:
    lEndTime = time.perf_counter()
    print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

# Download and save model
else:
    print(f"Failed. Tokenizer not found, proceeding to download.\n")
    print("Downloading tokenizer...", end=" ")
    lStartTime = time.perf_counter()
    GPT2Tokenizer.from_pretrained(teamName + modelName).save_pretrained(model_save_directory)
    lEndTime = time.perf_counter()
    print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

print("Creating pipeline...", end=" ")
lStartTime = time.perf_counter()
generator = pipeline('text-generation', model=model, tokenizer=tokenizer, device=0)
generator.model.config.pad_token_id = generator.model.config.eos_token_id
lEndTime = time.perf_counter()
print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

print("Sending model to GPU...", end=" ")
lStartTime = time.perf_counter()
model = model.to(device)
lEndTime = time.perf_counter()
print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

# ========== Main Loop ==========

evt = threading.Event()
result = None


def wait_until_prompt():
    global prompt
    while not prompt:
        # prompt = input("Set prompt: ")
        time.sleep(0.1)


def background_task():
    global result, prompt
    print("Looking for prompt...", end=" ")
    wait_until_prompt()
    result = prompt
    if result:
        print("Found one!\n", end=" ")
        evt.set()
    else:
        print()


def wait_until_escape():
    while not keyboard.is_pressed("escape"):
        time.sleep(0.1)


def check_quit_button():
    global shouldRun
    wait_until_escape()
    sys.exit("User has terminated the program")


t = threading.Thread(target=background_task)
q = threading.Thread(target=check_quit_button)

t.start()
q.start()


def set_prompt(new_prompt):
    global prompt
    prompt = new_prompt


def reset_wait():
    global prompt, evt, t
    prompt = None
    evt.clear()
    t = threading.Thread(target=background_task).start()


while shouldRun:
    time.sleep(0.1)
    # optional timeout
    # timeout = 0.1
    # evt.wait(timeout=timeout)

    if evt.is_set() and prompt:  # Proceed to generation if prompt has been found and isn't empty
        print("Prompt: " + str(result))

        print("Sending input to GPU...", end=" ")
        lStartTime = time.perf_counter()
        inputs = tokenizer(result, return_tensors="pt").to(device)
        outputs = model(**inputs)
        lEndTime = time.perf_counter()
        print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

        # Create a generator and generate output text
        print(f"Starting text generation with max length {max_length}...", end=" ")

        startTime = time.perf_counter()
        output = generator(result, do_sample=True, max_length=max_length)
        endTime = time.perf_counter()
        print(f"Completed after {endTime - startTime:0.2f} sec.\n")

        # ========== Print and Save Output ==========
        print("Output: " + output[0]["generated_text"])

        # File_object = open(modelName + r".txt", "a")
        # File_object.write(output[0]["generated_text"] + f" - Generation took {endTime - startTime:0.2f} sec.\n\n")
        # File_object.write("\n\n======================================================\n\n")
        # File_object.close()

        # Reset prompt and event
        reset_wait()

