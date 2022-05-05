from flask import Flask, request
from transformers import GPT2Tokenizer, GPTNeoForCausalLM, pipeline

import time

import torch

import os
import os.path


app = Flask(__name__)


class ModelData:
    model = None
    tokenizer = None
    generator = None
    prompt = None
    max_length = 100
    teamName = "EleutherAI/"
    modelName = "gpt-neo-1.3B"
    model_save_directory = "./" + modelName
    device = 0
    hasInitialized = False
    enable_model = True
    is_locked = False


@app.route('/hello/')
def hello_world():
    print("Hello World called!")
    return 'Hello World!'


@app.route('/parameter_test/', methods=['POST'])
def parameter_test():
    message = request.headers.get("message")
    print(message)
    m_len = request.headers.get("max_length")
    print(m_len)
    return message + str(m_len)


@app.route('/generate_narrative/', methods=['GET', 'POST'])
def generate_narrative():
    if not ModelData.is_locked:
        ModelData.is_locked = True
        request_prompt = request.headers.get("message")
        m_len = int(request.headers.get("max_length"))
        if m_len and m_len > 0:
            ModelData.max_length = m_len
            print("Changed max length to " + str(ModelData.max_length))
        print("generate_narrative called with prompt: " + request_prompt + "!")
        if not ModelData.hasInitialized:
            init()

        print("Sending input to GPU...", end=" ")
        start_time = time.perf_counter()
        inputs = ModelData.tokenizer(request_prompt, return_tensors="pt").to(ModelData.device)
        outputs = ModelData.model(**inputs)
        end_time = time.perf_counter()
        print(f"Completed after {end_time - start_time:0.2f} sec.\n")

        # Create a generator and generate output text
        print(f"Starting text generation with max length {ModelData.max_length}...", end=" ")
        start_time = time.perf_counter()
        while True:
            output = generate(request_prompt)
            if output[0]["generated_text"].index("</entry>") != output[0]["generated_text"].rindex("</entry>"):
                break
            else:
                print("Output invalid, trying again... ")

        end_time = time.perf_counter()
        print(f"Completed after {end_time - start_time:0.2f} sec.\n")

        ModelData.is_locked = False

        ret_string = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n<entries>" + output[0]["generated_text"]
        # ret_string find last occurrence of </entry> and paste </entries> after it
        ret_string = ret_string[:ret_string.rindex("</entry>") + len("</entry>")] + "</entries>"
        return ret_string


def generate(prompt):
    return ModelData.generator(prompt, do_sample=True, max_length=ModelData.max_length)


def init():
    # setting device on GPU if available, else CPU
    ModelData.deviceName = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print('Using device:', ModelData.deviceName)
    print()

    # Attempt to load model from disk
    print("Searching for model...", end=" ")

    # Model not present on disk. Download and save.
    if not os.path.isfile(ModelData.model_save_directory + "/pytorch_model.bin"):
        print(f"Failed. Model not found, proceeding to download.\n")
        print("Downloading model...", end=" ")
        start_time = time.perf_counter()
        GPTNeoForCausalLM.from_pretrained(ModelData.teamName + ModelData.modelName).save_pretrained(ModelData.model_save_directory)
        end_time = time.perf_counter()
        print(f"Completed after {end_time - start_time:0.2f} sec.\n")

    # Model is present on disk, don't download!
    else:
        print("Found! ")

    # Load model into RAM
    print("Loading model...", end=" ")
    start_time = time.perf_counter()
    ModelData.model = GPTNeoForCausalLM.from_pretrained(ModelData.model_save_directory)
    end_time = time.perf_counter()
    print(f"Completed after {end_time - start_time:0.2f} sec.\n")

    # Attempt to load tokenizer from disk
    print("Searching for tokenizer...", end=" ")

    # Tokenizer not present on disk. Download and save.
    if not os.path.isfile(ModelData.model_save_directory + "/tokenizer_config.json"):
        print(f"Failed. Tokenizer not found, proceeding to download.\n")
        print("Downloading tokenizer...", end=" ")
        start_time = time.perf_counter()
        GPT2Tokenizer.from_pretrained(ModelData.teamName + ModelData.modelName).save_pretrained(ModelData.model_save_directory)
        end_time = time.perf_counter()
        print(f"Completed after {end_time - start_time:0.2f} sec.\n")

    # Tokenizer is present on disk, don't download!
    else:
        print("Found! ")

    # Load tokenizer into RAM
    print("Loading tokenizer...", end=" ")
    start_time = time.perf_counter()
    ModelData.tokenizer = GPT2Tokenizer.from_pretrained(ModelData.model_save_directory, output_attentions=True)
    end_time = time.perf_counter()
    print(f"Completed after {end_time - start_time:0.2f} sec.\n")

    print("Creating pipeline...", end=" ")
    start_time = time.perf_counter()
    ModelData.generator = pipeline('text-generation', model=ModelData.model, tokenizer=ModelData.tokenizer, device=ModelData.device)
    ModelData.generator.model.config.pad_token_id = ModelData.generator.model.config.eos_token_id
    end_time = time.perf_counter()
    print(f"Completed after {end_time - start_time:0.2f} sec.\n")

    print("Sending model to GPU...", end=" ")
    start_time = time.perf_counter()
    ModelData.model = ModelData.model.to(ModelData.device)
    end_time = time.perf_counter()
    print(f"Completed after {end_time - start_time:0.2f} sec.\n")
    ModelData.hasInitialized = True


if __name__ == '__main__':
    if ModelData.enable_model:
        init()
    app.run(debug=False)

