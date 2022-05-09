from flask import Flask, request
from transformers import GPT2Tokenizer, GPTNeoForCausalLM, pipeline
from datasets import load_dataset

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
    num_runs = 1
    temperature = 0.9


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
        full_output = ""
        ModelData.is_locked = True
        request_prompt = request.headers.get("message")
        m_len = int(request.headers.get("max_length"))
        if m_len and m_len > 0:
            ModelData.max_length = m_len
            print("Changed max length to " + str(ModelData.max_length))
        else:
            ModelData.max_length = 500
        req_temp = float(request.headers.get("temperature"))
        if req_temp and req_temp > 0:
            ModelData.temperature = req_temp
        else:
            ModelData.temperature = 0.9
        req_runs = int(request.headers.get("num_runs"))
        if req_runs and req_runs > 0:
            ModelData.num_runs = req_runs
        else:
            ModelData.num_runs = 1
        print("generate_narrative called with prompt: " + request_prompt + "!")
        if not ModelData.hasInitialized:
            init()

        start_time = time.perf_counter()

        for x in range(ModelData.num_runs):
            # Create a generator and generate output text
            print(f"Starting text generation with max length {ModelData.max_length}...", end=" ")
            start_time = time.perf_counter()
            while True:
                output = generate(request_prompt)
                start_index = output.index("</entry>") + len("</entry>")
                end_index = output.rindex("</entry>") + len("</entry>")

                print("Start: " + str(start_index) + " || End: " + str(end_index))

                if start_index == end_index:
                    print("Output invalid, trying again... ")
                else:
                    full_output += output[start_index:end_index]
                    end_time = time.perf_counter()
                    print(f"Completed after {end_time - start_time:0.2f} sec.\n")
                    break

        ModelData.is_locked = False

        # ret_string = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n<entries>" + full_output
        ret_string = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n<entries>"

        final_index = full_output.rindex("</entry>") + len("</entry>")
        start_index = full_output.index("<entry>")
        end_index = full_output.index("</entry>") + len("</entry>")

        while True: # Add every (hopefully) complete xml entry
            if end_index == final_index:
                break

            ret_string += full_output[start_index:end_index]

            start_index = end_index
            end_index = full_output.index("</entry>", start_index) + len("</entry>")

        # full_output
        # ret_string find last occurrence of </entry> and paste </entries> after it
        # ret_string = ret_string[:(ret_string.rindex("</entry>") + len("</entry>"))]
        ret_string += "</entries>"
        return ret_string


def generate(prompt):
    input_ids = ModelData.tokenizer(prompt, return_tensors="pt").to(ModelData.device).input_ids
    gen_tokens = ModelData.model.generate(
        input_ids,
        do_sample=True,
        temperature=ModelData.temperature,
        min_length=(len(prompt) * 2),
        max_length=ModelData.max_length
    ).to(ModelData.device)
    return ModelData.tokenizer.batch_decode(gen_tokens)[0]


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

