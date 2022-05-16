from transformers import GPT2Tokenizer, GPTNeoForCausalLM, pipeline, TrainingArguments, Trainer
from datasets import load_dataset, load_metric
import random
import requests

import time

import torch
import numpy as np

import os
import os.path


class ModelData:
    model = None
    tokenizer = None
    generator = None
    prompt = None
    max_length = 100
    teamName = "EleutherAI/"
    modelName = "gpt-neo-125M"
    model_save_directory = "./" + modelName
    fine_tuned_model_directory = model_save_directory + "_fine-tuned"
    device = 0
    hasInitialized = False
    enable_model = True
    is_locked = False
    num_runs = 50
    temperature = 0.9
    dataset = None
    training_dataset = None
    validation_dataset = None


def tokenize_function(examples):
    inputs = [""] * len(examples["output"])
    for i in range(len(examples["output"])):
        inputs[i] = random.choice(examples["inputs"][i]["input"])
    return ModelData.tokenizer(inputs, examples["output"], padding="max_length", truncation=True)


def tokenize_training_function(examples):
    inputs = [""] * len(examples["output"])
    for i in range(len(examples["output"])):
        inputs[i] = random.choice(examples["inputs"][i]["input"])
        # inputs[i] = examples["inputs"][i]["input"]
    return ModelData.tokenizer(inputs, padding="max_length", truncation=True)


def tokenize_valid_function(examples):
    return ModelData.tokenizer(examples["output"], padding="max_length", truncation=True)


def concatenate(examples):
    block_size = 128
    concatenated_examples = {k: sum(examples[k], []) for k in examples.keys()}
    total_length = len(concatenated_examples[list(examples.keys())[0]])
    total_length = (total_length // block_size) * block_size
    result = {
        k: [t[i: i + block_size] for i in range(0, total_length, block_size)]
        for k, t in concatenated_examples.items()
    }
    examples["labels"] = examples["ouput_id"].copy()
    return result


def train():
    if not ModelData.validation_dataset:
        tokenized_valid_datasets = ModelData.dataset.map(tokenize_valid_function, batched=True)
        tokenized_training_datasets = ModelData.dataset.map(tokenize_training_function, batched=True)
        # lm_valid_datasets = tokenized_valid_datasets.map(
        #     concatenate,
        #     batched=True,
        #     batch_size=1000
        # )

        ModelData.tokenizer.decode(tokenized_valid_datasets["train"][1]["input_ids"])
        lm_valid_datasets = tokenized_valid_datasets.with_format("torch")
        ModelData.validation_dataset = lm_valid_datasets

    tokenized_training_datasets = ModelData.dataset.map(tokenize_training_function, batched=True,
                                                        remove_columns=["id", "inputs"])
    # lm_train_datasets = tokenized_training_datasets.map(
    #     concatenate,
    #     batched=True,
    #     batch_size=1000
    # )
    ModelData.tokenizer.decode(tokenized_training_datasets["train"][1]["input_ids"])
    lm_train_datasets = tokenized_training_datasets.with_format("torch")
    ModelData.training_dataset = tokenized_training_datasets
    tokenized_training_datasets["train"]["labels"] = tokenized_valid_datasets["train"]["input_id"].copy()
    args = TrainingArguments(
        output_dir="test_trainer-ds",
        per_device_train_batch_size=8,
        per_device_eval_batch_size=8,
        evaluation_strategy="epoch",
        learning_rate=2e-5,
        weight_decay=0.01,
        push_to_hub=False,
    )
    torch.cuda.empty_cache()
    trainer = Trainer(
        model=ModelData.model,
        args=args,
        train_dataset=lm_train_datasets["train"],
        eval_dataset=ModelData.validation_dataset["train"]
    )
    trainer.train()
    trainer.save_model(ModelData.model_save_directory + "_fine-tuned")

    return "Success"


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

    # print("Creating pipeline...", end=" ")
    # start_time = time.perf_counter()
    # ModelData.generator = pipeline('text-generation', model=ModelData.model, tokenizer=ModelData.tokenizer, device=ModelData.device)
    # ModelData.generator.model.config.pad_token_id = ModelData.generator.model.config.eos_token_id
    # end_time = time.perf_counter()
    # print(f"Completed after {end_time - start_time:0.2f} sec.\n")

    # print("Sending model to GPU...", end=" ")
    # start_time = time.perf_counter()
    # ModelData.model = ModelData.model.to(ModelData.device)
    # end_time = time.perf_counter()
    # print(f"Completed after {end_time - start_time:0.2f} sec.\n")
    ModelData.hasInitialized = True

    base_url = "https://github.com/Hahoolah/nardat/blob/main/"
    # print(base_url)
    r = requests.get('https://raw.githubusercontent.com/Hahoolah/nardat/main/data.json')
    # print(r.text)
    with open("./datasets/nardat/data.json", 'w', encoding="utf-8") as file:
        file.write(r.text)
    r = requests.get('https://raw.githubusercontent.com/Hahoolah/nardat/main/dataset_infos.json')
    with open("./datasets/nardat/dataset_infos.json", 'w', encoding="utf-8") as file:
        file.write(r.text)
    r = requests.get('https://raw.githubusercontent.com/Hahoolah/nardat/main/nardat.py')
    with open("./datasets/nardat/nardat.py", 'w', encoding="utf-8") as file:
        file.write(r.text)
    ModelData.dataset = load_dataset("./datasets/nardat")


if ModelData.enable_model:
    init()
    for i in range(ModelData.num_runs):
        if i > 0:
            print("Loading latest training result!\n")
            # Load model into RAM
            print("Loading model...", end=" ")
            start_time = time.perf_counter()
            ModelData.model = GPTNeoForCausalLM.from_pretrained(ModelData.fine_tuned_model_directory)
            end_time = time.perf_counter()
            print(f"Completed after {end_time - start_time:0.2f} sec.\n")
        print("Training! Iteration number: " + str(i + 1))
        train()

