import copy
import os
import xml.etree.ElementTree as ET
from github import Github
from github import InputGitTreeElement
import shutil
import random


def upload_to_github():
    token = "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"
    g = Github(token)
    repo = g.get_user().get_repo('nardat')  # repo name
    file_names = [
        'dataset_infos.json',
        'train.json',
        'validate.json',
        'nardat.py'
    ]
    commit_message = 'python commit'
    for f in file_names:
        contents = repo.get_contents(f)
        print("Updating " + f)
        content = ""
        with open('./nardat/' + f, 'r', encoding="utf-8") as file:
            for x in file:
                content += x

        repo.update_file(contents.path, commit_message, content, contents.sha)


def increment_index(array, i):
    if array[i] == 0:
        array[i] = 1
    else:
        if i > 0:
            array[i] = 0
            increment_index(array, i - 1)
        else:
            for n in range(len(array)):
                array[n] = 0


def make_datasets():
    # make_dataset("data")
    make_dataset("train")
    make_dataset("validate")
    upload_to_github()


def make_dataset(task_type):
    tree = ET.parse("./datasets/src/" + task_type + ".xml")
    root = tree.getroot()
    print("===== Generating '", task_type, "' dataset! =====")
    print("data contains ", len(root), " entries. Generating data for entry number ")
    output = "{\n\t\"data\": [\n"
    i = 0

    for child in root:
        child_str = ""
        if i % 10 == 0:
            print(i, "/", len(root))
        if i > 0:
            child_str += ",\n"
        child_str += "\t\t{\n\t\t\t\"id\": \"" + str(i) + "\",\n"
        output_str = ET.tostring(child, encoding="unicode")
        output_str = output_str.replace('\t', '')
        output_str = output_str.replace('\n', '')
        output_str = output_str.replace('    ', '')
        child_str += "\t\t\t\"output\": \"" + output_str + "\",\n\t\t\t\"inputs\": [\n"

        elem_arr = [0] * len(child)

        entry = copy.deepcopy(child)
        entry.text = ""
        entry.tail = ""
        j = 0
        for e in entry:
            e.tail = ""
            e.text = " "
            j += 1
        output_str = ET.tostring(entry, encoding="unicode")
        output_str = output_str.replace("> <", "><")
        child_str += "\t\t\t\t{\n\t\t\t\t\t\"input\": \"" + output_str + "\"\n\t\t\t\t}"

        while elem_arr != [1] * 8:
            increment_index(elem_arr, elem_arr.__len__() - 1)
            entry = copy.deepcopy(child)
            entry.text = ""
            entry.tail = ""
            j = 0
            for e in entry:
                e.tail = ""
                if elem_arr[j] == 0:
                    e.text = " "
                j += 1
            output_str = ET.tostring(entry, encoding="unicode")
            output_str = output_str.replace("> <", "><")
            child_str += ",\n\t\t\t\t{\n\t\t\t\t\t\"input\": \"" + output_str + "\"\n\t\t\t\t}"
            #if elem_arr[len(elem_arr) - 1] == 0:

            # if random.randrange(10) < 6:
        child_str += "\n\t\t\t]\n\t\t}"
        i += 1
        output += child_str

    output += "\n\t]\n}"

    with open('./nardat/' + task_type + '.json', 'w', encoding="utf-8") as output_file:
        output = output.replace('\t', '')
        output = output.replace('\n', '')
        output = output.replace('    ', '')
        output_file.write(output)


make_datasets()

