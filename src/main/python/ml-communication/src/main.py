from flask import Flask, jsonify, request, abort
import json
import pandas as pd
import numpy as np

import io
import os
import os.path
from os import listdir
from os import path

from PIL import Image, ImageDraw
import matplotlib.pyplot as plt

from xml.dom import minidom

from EDD.Room import CustomTiles, TileData, Room
from EDD.RoomRenderer import RoomRenderer
from designerPersona.DesignerPersonaModel import DesignerPersonaModel

# print(os.listdir("../"))
designer_persona = DesignerPersonaModel()
renderer = RoomRenderer("../../../resources/graphics/tiles", 0.01)
app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello World!'


@app.route('/add/', methods=['POST'])
def add_numbers():
    if request.method == 'POST':
        decoded_data = request.data.decode('utf-8')
        params = json.loads(decoded_data)
        print(params)
        print(type(params["text"]))
        print(type(params["float"]))
        print(params["float"])
        print(type(params["0"]))
        b = float(params["float"])
        print(b + b)
        return jsonify({'sum': params['0'] + params['1']})


@app.route('/predict', methods=['POST'])
def predict():
    json_ = request.json
    decoded_data = request.data.decode('utf-8')
    print(decoded_data)
    room = roomFromXML(decoded_data)
    print(room.test_data.shape)
    prediction = designer_persona.transform_predict(room.test_data)

    print("PREDICTED AS ", prediction)
    return jsonify({'prediction': list(prediction)})


@app.route('/get_room/', methods=['POST'])
def get_room():
    print("GET ROOM IS CALLED!")
    json_ = request.json
    print(json_)
    decoded_data = request.data.decode('utf-8')
    print(decoded_data)
    room = roomFromXML(decoded_data)
    print(room.test_data.shape)
    a = designer_persona.transform_predict(room.test_data)

    print("PREDICTED AS ", a)

    # mydoc = minidom.parseString(decoded_data)
    #
    # print(mydoc.getElementsByTagName('Login'))
    # print(mydoc.getElementsByTagName('Login')[0])
    # print(mydoc.getElementsByTagName('Login')[0].data)

    return jsonify(request.json)
    # query_df = pd.DataFrame(json_)
    # query = pd.get_dummies(query_df)
    # prediction = 2
    # return jsonify({'prediction': list(prediction)})


@app.route('/print_steps/', methods=['POST'])
def print_steps():
    # print("GET ROOM IS CALLED!")

    # We collect the json from EDD, and all the steps till now
    json_ = request.json
    raw_xml_room_steps = json_["rooms"]

    # Create the rooms from the xmls
    room_steps = []
    counter = 0
    for xml_room in raw_xml_room_steps:
        room_steps.append(roomFromXML(xml_room, counter).test_data)
        counter = counter + 1

    # Convert to Numpy array and squeeze dim, so it can be used by the scikit model.
    room_steps = np.array(room_steps)
    room_steps = room_steps.squeeze(axis=1)

    designer_persona.printStepsBackgroundLabeled(room_steps, True)

    return "cool"


@app.route('/get_rooms_fake/', methods=['POST'])
def get_rooms_fake():
    # print("GET ROOM IS CALLED!")
    # We collect the json from EDD, and the possible rooms
    json_ = request.json

    if json_ is None:
        abort(404, description="Resource not found")

    json_rooms = json_["rooms"]
    json_rooms_id = json_["rooms_id"]
    json_rooms_width = json_["rooms_width"]
    json_rooms_height = json_["rooms_height"]

    # Create the rooms from the xmls

    rooms = []
    for i in range(len(json_rooms)):
        rooms.append(roomFromMatrix(json_rooms[i], json_rooms_width[i], json_rooms_height[i], json_rooms_id[i]).test_data)

    #
    # for json_room in json_rooms:
    #     rooms.append(roomFromXML(json_room).test_data)

    # Convert to Numpy array and squeeze dim, so it can be used by the scikit model.
    rooms = np.array(rooms)
    rooms = rooms.squeeze(axis=1)

    # Pass the room array to get predictions
    predictions = designer_persona.transform_predict(rooms)

    # predictions = np.random.randint(0, 12, (len(json_rooms)))

    # print("PREDICTED AS ", predictions)

    return jsonify({'prediction': predictions.tolist()})


@app.route('/get_rooms/', methods=['POST'])
def get_rooms():
    # print("GET ROOM IS CALLED!")

    json_ = request.json

    # if json_ is None:
    #     abort(403, description="Resource not found")

    json_rooms = json_["rooms"]
    json_rooms_id = json_["rooms_id"]
    json_rooms_width = json_["rooms_width"]
    json_rooms_height = json_["rooms_height"]


    # Create the rooms from the xmls

    rooms = []
    for i in range(len(json_rooms)):
        rooms.append(roomFromMatrix(json_rooms[i], json_rooms_width[i], json_rooms_height[i], json_rooms_id[i]).test_data)

    #
    # for json_room in json_rooms:
    #     rooms.append(roomFromXML(json_room).test_data)

    # Convert to Numpy array and squeeze dim, so it can be used by the scikit model.
    rooms = np.array(rooms)
    rooms = rooms.squeeze(axis=1)

    # pred = []
    # for i in range(len(json_rooms)):
    #     pred.append(i)
    #
    # return jsonify({'prediction': pred})

    # Pass the room array to get predictions
    predictions = designer_persona.transform_predict(rooms)

    return jsonify({'prediction': predictions.tolist()})

    # We collect the json from EDD, and the possible rooms
    # json_ = request.json
    # some = json_["rooms"]
    #
    # # Create the rooms from the xmls
    # rooms = []
    # for s in some:
    #     rooms.append(roomFromXML(s).test_data)
    #
    # # Convert to Numpy array and squeeze dim, so it can be used by the scikit model.
    # rooms = np.array(rooms)
    # rooms = rooms.squeeze(axis=1)
    #
    # # Pass the room array to get predictions
    # predictions = designer_persona.transform_predict(rooms)
    #
    # print("PREDICTED AS ", predictions)
    #
    # return jsonify({'prediction': predictions.tolist()})


def roomFromMatrix(room_matrix, width, height, room_id):
    room_from_matrix = Room(room_id, width, height, None)

    transformed_matrix = np.array(list(room_matrix), dtype=int)
    transformed_matrix = np.reshape(transformed_matrix, (height, width))

    room_from_matrix.setTilesFromMatrix(transformed_matrix)

    # print(room_from_matrix.matrix)
    # out_image = "./"
    canvas = Image.new("RGB", (int(room_from_matrix.width * renderer.paint_step),
                               int(room_from_matrix.height * renderer.paint_step)))  # The issue is drawing this!! We need to work on this!
    renderer.drawPixels(canvas, room_from_matrix.matrix)
    # canvas.save(out_image + 'rpixelRoom' + ".png")
    canvas = canvas.resize((130, 70), Image.NEAREST)
    room_from_matrix.addMinPixel(np.array(canvas))

    # canvas.save(out_image + 'reducedpixelRoom' + ".png")

    return room_from_matrix


def roomFromXML(file, counter=0):
    # parse an xml file by name
    mydoc = minidom.parseString(file)
    room_info = mydoc.getElementsByTagName('Room')

    custom_tiles_xml = mydoc.getElementsByTagName('Custom')
    custom_tiles = []

    for custom_tile in custom_tiles_xml:
        custom_tiles.append(CustomTiles(int(custom_tile.attributes['centerX'].value),
                                        int(custom_tile.attributes['centerY'].value),
                                        TileData[custom_tile.attributes['value'].value].value))

    xmlRoom = Room(room_info[0].attributes['ID'].value, int(room_info[0].attributes['width'].value),
                   int(room_info[0].attributes['height'].value), custom_tiles)

    xmlTiles = mydoc.getElementsByTagName('Tile')
    for xmlTile in xmlTiles:
        xmlRoom.setTile(int(xmlTile.attributes['PosX'].value), int(xmlTile.attributes['PosY'].value),
                        TileData[xmlTile.attributes['value'].value].value)

    # print(xmlRoom.matrix)
    out_image = "./"
    canvas = Image.new("RGB", (int(xmlRoom.width * renderer.paint_step),
                               int(xmlRoom.height * renderer.paint_step)))  # The issue is drawing this!! We need to work on this!
    renderer.drawPixels(canvas, xmlRoom)
    canvas = canvas.resize((130, 70), Image.NEAREST)
    xmlRoom.addMinPixel(np.array(canvas))

    # Uncomment to save each room!
    # save_room_imgs(xmlRoom, counter)

    # canvas.save(out_image + 'reducedpixelRoom' + ".png")
    # testDataSet[cur_usr][td[i]][ind].addMinPixel(numpy.array(im))

    return xmlRoom


def save_room_imgs(xmlRoom, counter=0):
    out_image = "./"
    canvas = Image.new("RGB", (int(xmlRoom.width * renderer.paint_step), int(xmlRoom.height * renderer.paint_step)))
    minimal_canvas = Image.new("RGB", (int(xmlRoom.width), int(xmlRoom.height)))

    renderer.drawPixels(canvas, xmlRoom)
    canvas.save(out_image + 'pixelRoom' + str(counter) + ".png")
    renderer.drawPixels(canvas, xmlRoom.matrix)
    canvas.save(out_image + 'pixelMatrix' + str(counter) +".png")
    renderer.drawTiles(canvas, xmlRoom)
    canvas.save(out_image + 'tilesRoom' + str(counter) +".png")
    renderer.drawTiles(canvas, xmlRoom.matrix)
    canvas.save(out_image + 'tilesMatrix' + str(counter) +".png")

    renderer.drawPixels(minimal_canvas, xmlRoom, True)
    minimal_canvas.save(out_image + 'simplePixelRoom' + str(counter) +".png")
    renderer.drawPixels(minimal_canvas, xmlRoom.matrix, True)
    minimal_canvas.save(out_image + 'simplePixelMatrix' + str(counter) +".png")


if __name__ == '__main__':

    designer_persona.load_scaler("../../../resources/models/despers_scaler.pkl")
    designer_persona.load_pca("../../../resources/models/despers_pca.pkl")
    designer_persona.load_base_dataset("../../../resources/models/despers_pca_dataset.pkl")
    designer_persona.load_model("../../../resources/models/despers_classifier.pkl")
    # designer_persona.print_background()
    app.run(debug=False)
