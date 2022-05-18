import tokenizers
from flask import Flask, request
from transformers import GPT2Tokenizer, GPTNeoForCausalLM, pipeline, TrainingArguments, Trainer
from datasets import load_dataset, load_metric
import random

import time

import torch
import numpy as np

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
    fine_tuned_model_directory = model_save_directory + "_fine-tuned"
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
        ModelData.is_locked = True
        request_prompt = request.headers.get("message")
        m_len = int(request.headers.get("max_length"))
        ModelData.num_runs = int(request.headers.get("num_runs"))
        # ModelData.num_runs = 4
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

        full_output = ""
        while full_output == "":
            full_output = generation_attempt(request_prompt)

        ModelData.is_locked = False

        ret_string = "<?xml version = \"1.0\" encoding = \"UTF-8\"?>\n<entries>"

        try:
            final_index = full_output.rindex("</entry>") + len("</entry>")
            start_index = full_output.index("<entry>")
            end_index = full_output.index("</entry>") + len("</entry>")

            while True:  # Add every (hopefully) complete xml entry
                ret_string += full_output[start_index:end_index]

                if end_index == final_index:
                    break
                start_index = end_index
                end_index = full_output.index("</entry>", start_index) + len("</entry>")
            ret_string += "</entries>"
        except:
            print("Error in try block at line 82")
            if full_output == "":
                print("full_output is empty")
            else:
                print(full_output)

        # full_output
        # ret_string find last occurrence of </entry> and paste </entries> after it
        # ret_string = ret_string[:(ret_string.rindex("</entry>") + len("</entry>"))]
        print("Returning ", ret_string, " to request source")
        return ret_string


def generation_attempt(request_prompt):
    ret_str = ""
    start_time = time.perf_counter()
    for x in range(ModelData.num_runs):
        # Create a generator and generate output text
        print(f"Starting text generation with max length {ModelData.max_length}...", end=" ")
        while True:
            output = generate(request_prompt)
            prompt_end_index = output[0]["generated_text"].index("</entry>") + len("</entry>")
            gen_text = output[0]["generated_text"][prompt_end_index:]
            try:
                start_index = gen_text.index("<entry>")
                end_index = gen_text.index("</entry>") + len("</entry>")
                print("Found entry between ", start_index, " and ", end_index)
                last_index = gen_text.rindex("</entry>") + len("</entry>")
                # Find name tag
                gen_text[gen_text[start_index:].index("<name>"):].index("</name>")
                print("Found name tag")
                # Find age tag
                gen_text[gen_text[start_index:].index("<age>"):].index("</age>")
                print("Found age tag")
                # Find gender tag
                gen_text[gen_text[start_index:].index("<gender>"):].index("</gender>")
                print("Found gender tag")
                # Find race tag
                gen_text[gen_text[start_index:].index("<race>"):].index("</race>")
                print("Found race tag")
                # Find appearance tag
                gen_text[gen_text[start_index:].index("<appearance>"):].index("</appearance>")
                print("Found appearance tag")
                # Find loves tag
                gen_text[gen_text[start_index:].index("<loves>"):].index("</loves>")
                print("Found loves tag")
                # Find hates tag
                gen_text[gen_text[start_index:].index("<hates>"):].index("</hates>")
                print("Found hates tag")
                # Find narrative tag
                gen_text[gen_text[start_index:].index("<narrative>"):].index("</narrative>")
                print("Found narrative tag")

                ret_str += gen_text[start_index:end_index]
                break
            except:
                print(gen_text)
                print("Output invalid, trying again... ")
                continue

            # print("Start: " + str(start_index) + " || End: " + str(end_index))

            # if start_index == last_index:
            #     print("Output invalid, trying again... ")
            # else:
            #     ret_str += gen_text[start_index:last_index]
            #     break

    end_time = time.perf_counter()
    print(f"Completed after {end_time - start_time:0.2f} sec.\n")
    print("generation attempt returned ", ret_str)
    return ret_str


def generate(prompt):
    # input_ids = ModelData.tokenizer(prompt, return_tensors="pt").to(ModelData.device).input_ids
    # gen_tokens = ModelData.model.generate(
    #     input_ids,
    #     do_sample=True,
    #     # temperature=ModelData.temperature,
    #     min_length=(len(prompt) * 2),
    #     max_length=ModelData.max_length
    # ).to(ModelData.device)
    # return ModelData.tokenizer.batch_decode(gen_tokens)[0]
    return ModelData.generator(prompt, do_sample=True, max_length=ModelData.max_length)


def init():
    # setting device on GPU if available, else CPU
    ModelData.deviceName = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
    print('Using device:', ModelData.deviceName)
    print()

    # Attempt to load model from disk
    print("Searching for model...", end=" ")

    # Model not present on disk. Download and save.
    if not os.path.isfile(ModelData.fine_tuned_model_directory + "/pytorch_model.bin"):
        if not os.path.isfile(ModelData.model_save_directory + "/pytorch_model.bin"):
            print(f"Failed. Model not found, proceeding to download.\n")
            print("Downloading model...", end=" ")
            start_time = time.perf_counter()
            GPTNeoForCausalLM.from_pretrained(ModelData.teamName + ModelData.modelName).save_pretrained(
                ModelData.model_save_directory)
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
    else:
        print("Found fine-tuned version! ")
        # Load model into RAM
        print("Loading model...", end=" ")
        start_time = time.perf_counter()
        ModelData.model = GPTNeoForCausalLM.from_pretrained(ModelData.fine_tuned_model_directory)
        end_time = time.perf_counter()
        print(f"Completed after {end_time - start_time:0.2f} sec.\n")

    # Attempt to load tokenizer from disk
    print("Searching for tokenizer...", end=" ")

    # Tokenizer not present on disk. Download and save.
    if not os.path.isfile(ModelData.model_save_directory + "/tokenizer_config.json"):
        print(f"Failed. Tokenizer not found, proceeding to download.\n")
        print("Downloading tokenizer...", end=" ")
        start_time = time.perf_counter()
        GPT2Tokenizer.from_pretrained(ModelData.teamName + ModelData.modelName).save_pretrained(
            ModelData.model_save_directory)
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

    ModelData.tokenizer.pad_token = ModelData.tokenizer.eos_token

    print("Creating pipeline...", end=" ")
    start_time = time.perf_counter()
    ModelData.generator = pipeline('text-generation', model=ModelData.model, tokenizer=ModelData.tokenizer,
                                   device=ModelData.device)
    ModelData.generator.model.config.pad_token_id = ModelData.generator.model.config.eos_token_id
    end_time = time.perf_counter()
    print(f"Completed after {end_time - start_time:0.2f} sec.\n")
    #
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
