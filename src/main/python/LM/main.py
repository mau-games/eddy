import time

import torch
import threading
import keyboard
import sys
import os

from transformers import GPT2Tokenizer, GPTNeoForCausalLM, pipeline

# check feature-DesignerPersonas/src/main/python & java
# can use flask server to send data between java and python through localhost
# create state to interact with flask server

# ========== Vars ==========

shouldRun = True
prompt = "I saw an orc and instantly froze. "
# prompt = None
max_length = 100
teamName = "EleutherAI/"
modelName = "gpt-neo-1.3B"
model_save_directory = "./" + modelName
hasReceivedInput = False
shouldSaveOutputToDisk = False
model = None
tokenizer = None
device = 0

# ========== Init ==========

# setting device on GPU if available, else CPU
deviceName = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print('Using device:', deviceName)
print()


# Attempt to load model from disk
print("Searching for model...", end=" ")

# Model not present on disk. Download and save.
if not os.path.isfile(model_save_directory + "/pytorch_model.bin"):
    print(f"Failed. Model not found, proceeding to download.\n")
    print("Downloading model...", end=" ")
    lStartTime = time.perf_counter()
    GPTNeoForCausalLM.from_pretrained(teamName + modelName).save_pretrained(model_save_directory)
    lEndTime = time.perf_counter()
    print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

# Model is present on disk, don't download!
else:
    print("Found! ")

# Load model into RAM
print("Loading model...", end=" ")
lStartTime = time.perf_counter()
model = GPTNeoForCausalLM.from_pretrained(model_save_directory)
lEndTime = time.perf_counter()
print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")


# Attempt to load tokenizer from disk
print("Searching for tokenizer...", end=" ")

# Tokenizer not present on disk. Download and save.
if not os.path.isfile(model_save_directory + "/tokenizer_config.json"):
    print(f"Failed. Tokenizer not found, proceeding to download.\n")
    print("Downloading tokenizer...", end=" ")
    lStartTime = time.perf_counter()
    GPT2Tokenizer.from_pretrained(teamName + modelName).save_pretrained(model_save_directory)
    lEndTime = time.perf_counter()
    print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

# Tokenizer is present on disk, don't download!
else:
    print("Found! ")

# Load tokenizer into RAM
print("Loading tokenizer...", end=" ")
lStartTime = time.perf_counter()
tokenizer = GPT2Tokenizer.from_pretrained(model_save_directory, output_attentions=True)
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
# q = threading.Thread(target=check_quit_button)

t.start()
# q.start()


def receive_request(new_prompt, new_length, new_should_save):
    global prompt, max_length, shouldSaveOutputToDisk
    prompt = new_prompt
    max_length = new_length
    shouldSaveOutputToDisk = new_should_save


def reset():
    global prompt, evt, t, shouldRun
    sys.exit()

    prompt = None
    evt.clear()
    t = threading.Thread(target=background_task).start()


while shouldRun:
    time.sleep(0.1)

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

        if shouldSaveOutputToDisk:
            File_object = open(modelName + r".txt", "a")
            File_object.write(output[0]["generated_text"] + f" - Generation took {endTime - startTime:0.2f} sec.\n\n")
            File_object.write("\n\n======================================================\n\n")
            File_object.close()

        # Reset prompt and event
        reset()
