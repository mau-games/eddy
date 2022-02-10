from flask import Flask, jsonify, request
import json
import pandas as pd
import numpy as np

import io
import os
import os.path
from os import listdir
from os import path

from PIL import Image, ImageDraw

from xml.dom import minidom

from EDD.Room import CustomTiles, TileData, Room
from EDD.RoomRenderer import RoomRenderer
from designerPersona.DesignerPersonaModel import DesignerPersonaModel

# print(os.listdir("../"))
designer_persona = DesignerPersonaModel()
renderer = RoomRenderer("../../../resources/graphics/tiles", 1.0)
app = Flask(__name__)


@app.route('/')
def hello_world():
    return 'Hello World!'

@app.route('/add/', methods = ['POST'])
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

@app.route('/get_rooms/', methods=['POST'])
def get_rooms():
    # print("GET ROOM IS CALLED!")
    json_ = request.json
    print(json_)
    # print(json_["rooms"])

    some = json_["rooms"]
    rooms = []

    for s in some:
        rooms.append(roomFromXML(s).test_data)

    rooms = np.array(rooms)
    # print("dimensions and shape: ", rooms.shape)
    # if rooms.ndim > 2:
    #     rooms = rooms.squeeze()

    rooms = rooms.squeeze(axis=1)

    a = designer_persona.transform_predict(rooms)

    print("PREDICTED AS ", a)

    # some = request.form.getlist("rooms[]")
    #
    # decoded_data = request.data.decode('utf-8')
    # print(decoded_data)
    # room = roomFromXML(decoded_data)
    # print(room.test_data.shape)
    # a = designer_persona.transform_predict(room.test_data)
    #
    # print("PREDICTED AS ", a)



    # mydoc = minidom.parseString(decoded_data)
    #
    # print(mydoc.getElementsByTagName('Login'))
    # print(mydoc.getElementsByTagName('Login')[0])
    # print(mydoc.getElementsByTagName('Login')[0].data)


    return jsonify({'prediction': a.tolist()})
    # return jsonify(request.json)

    # query_df = pd.DataFrame(json_)
    # query = pd.get_dummies(query_df)
    # prediction = 2
    # return jsonify({'prediction': list(prediction)})

def roomFromXML(file):

    # parse an xml file by name
    mydoc = minidom.parseString(file)
    room_info = mydoc.getElementsByTagName('Room')

    custom_tiles_xml = mydoc.getElementsByTagName('Custom')
    custom_tiles = []

    for custom_tile in custom_tiles_xml:
        custom_tiles.append(CustomTiles(int(custom_tile.attributes['centerX'].value),
                                        int(custom_tile.attributes['centerY'].value),
                                        TileData[custom_tile.attributes['value'].value].value))

    xmlRoom = Room(room_info[0].attributes['ID'].value, int(room_info[0].attributes['width'].value), int(room_info[0].attributes['height'].value), custom_tiles)

    xmlTiles = mydoc.getElementsByTagName('Tile')
    for xmlTile in xmlTiles:
        xmlRoom.setTile(int(xmlTile.attributes['PosX'].value), int(xmlTile.attributes['PosY'].value), TileData[xmlTile.attributes['value'].value].value)

    # print(xmlRoom.matrix)
    out_image = "./"
    canvas = Image.new("RGB", (int(xmlRoom.width * renderer.paint_step), int(xmlRoom.height * renderer.paint_step)))
    renderer.drawPixels(canvas, xmlRoom)
    canvas = canvas.resize((130, 70), Image.NEAREST)
    xmlRoom.addMinPixel(np.array(canvas))
    # canvas.save(out_image + 'reducedpixelRoom' + ".png")
    # testDataSet[cur_usr][td[i]][ind].addMinPixel(numpy.array(im))

    return xmlRoom


def save_room_imgs(xmlRoom):
    out_image = "./"
    canvas = Image.new("RGB", (int(xmlRoom.width * renderer.paint_step), int(xmlRoom.height * renderer.paint_step)))
    minimal_canvas = Image.new("RGB", (int(xmlRoom.width), int(xmlRoom.height)))

    renderer.drawPixels(canvas, xmlRoom)
    canvas.save(out_image + 'pixelRoom' + ".png")
    renderer.drawPixels(canvas, xmlRoom.matrix)
    canvas.save(out_image + 'pixelMatrix' + ".png")
    renderer.drawTiles(canvas, xmlRoom)
    canvas.save(out_image + 'tilesRoom' + ".png")
    renderer.drawTiles(canvas, xmlRoom.matrix)
    canvas.save(out_image + 'tilesMatrix' + ".png")

    renderer.drawPixels(minimal_canvas, xmlRoom, True)
    minimal_canvas.save(out_image + 'simplePixelRoom' + ".png")
    renderer.drawPixels(minimal_canvas, xmlRoom.matrix, True)
    minimal_canvas.save(out_image + 'simplePixelMatrix' + ".png")

if __name__ == '__main__':

    print(os.listdir("../"))

    designer_persona.load_scaler("../../../resources/models/despers_scaler.pkl")
    designer_persona.load_pca("../../../resources/models/despers_pca.pkl")
    designer_persona.load_base_dataset("../../../resources/models/despers_pca_dataset.pkl")
    designer_persona.load_model("../../../resources/models/despers_classifier.pkl")
    # designer_persona.print_background()
    app.run(debug=True)
