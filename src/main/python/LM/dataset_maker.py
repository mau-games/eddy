import copy
import xml.etree.ElementTree as ET


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


tree = ET.parse("./dataset/raw.xml")
root = tree.getroot()

output = "{\n\t\"data\": [\n"
i = 0

for child in root:
    if i > 0:
        output += ",\n"
    output += "\t\t{\n\t\t\t\"id\": \"" + str(i) + "\",\n"
    output_str = ET.tostring(child, encoding="unicode")
    output_str = output_str.replace('\t', '')
    output_str = output_str.replace('\n', '')
    output += "\t\t\t\"output\": \"" + output_str + "\",\n\t\t\t\"inputs\": [\n"

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
    output += "\t\t\t\t{\n\t\t\t\t\t\"input\": " + output_str + "\n\t\t\t\t}"

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
        output += ",\n\t\t\t\t{\n\t\t\t\t\t\"input\": " + output_str + "\n\t\t\t\t}"

    output += "\n\t\t\t]\n\t\t}"
    i += 1

output += "\n\t]\n}"

with open('./dataset/data.json', 'w') as output_file:
    output_file.write(output)

