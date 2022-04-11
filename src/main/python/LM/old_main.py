import time
import torch

from transformers import GPT2Tokenizer, GPTNeoForCausalLM, pipeline

# prompt = "Welcome Captain Janeway, I apologize for the delay."
prompt = "I saw an orc"
start_length = 500
max_length = 1000
increment = 100
num_iterations = 3
teamName = "EleutherAI/"
modelName = "gpt-neo-1.3B"
model_save_directory = "./" + modelName

# ========== Offline Code ==========
# Download and save model
# GPTNeoForCausalLM.from_pretrained(teamName + modelName).save_pretrained(model_save_directory)
# GPT2Tokenizer.from_pretrained(teamName + modelName).save_pretrained(model_save_directory)

# setting device on GPU if available, else CPU
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
print('Using device:', device)
print()


# Load model and tokenizer from disk
print("Loading model...", end=" ")
lStartTime = time.perf_counter()
model = GPTNeoForCausalLM.from_pretrained(model_save_directory)
lEndTime = time.perf_counter()
print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")
print("Loading Tokenizer...", end=" ")
lStartTime = time.perf_counter()
tokenizer = GPT2Tokenizer.from_pretrained(model_save_directory)
lEndTime = time.perf_counter()
print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")
print("Creating pipeline...", end=" ")
lStartTime = time.perf_counter()
generator = pipeline('text-generation', model=model, tokenizer=tokenizer, device=0)
generator.model.config.pad_token_id = generator.model.config.eos_token_id
lEndTime = time.perf_counter()
print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")
print("Sending stuff to GPU...", end=" ")
lStartTime = time.perf_counter()
inputs = tokenizer(prompt, return_tensors="pt").to(device)
model = model.to(device)
outputs = model(**inputs)
lEndTime = time.perf_counter()
print(f"Completed after {lEndTime - lStartTime:0.2f} sec.\n")

for curr_length in range(start_length, max_length + 1, increment):
    for i in range(num_iterations):
        # Create a generator and generate output text
        print(f"Starting text generation #{i + 1} with length {curr_length}...", end=" ")

        startTime = time.perf_counter()
        output = generator(prompt, do_sample=True, max_length=curr_length)
        endTime = time.perf_counter()
        print(f"Completed after {endTime - startTime:0.2f} sec.\n")

        # ========== Print and Save Output ==========
        print("Output: " + output[0]["generated_text"])

        File_object = open(modelName + r".txt", "a")
        File_object.write(f"Length: {curr_length} - Number:{i + 1} - Generation took {endTime - startTime:0.2f} sec.\n\n")
        # File_object.write(output[0]["generated_text"])
        File_object.write("\n\n======================================================\n\n")
        File_object.close()

# ========== Online Code ==========
# generator = pipeline('text-generation', model='KoboldAI/GPT-Neo-2.7B-Janeway')
# output = generator(prompt, do_sample=True, max_length=100)
# print(generator("Welcome Captain Janeway, I apologize for the delay.", do_sample=True, min_length=50))
