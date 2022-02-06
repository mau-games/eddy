from PIL import Image, ImageDraw
import numpy as np

from EDD.Room import Room


class RoomRenderer:
    def __init__(self, root, scaling_factor):

        self.t0_path = "../../resources/graphics/tiles/floor.png"
        self.t1_path = "../../resources/graphics/tiles/wall.png"
        self.t2_path = "../../resources/graphics/tiles/treasure.png"
        self.t3_path = "../../resources/graphics/tiles/enemy.png"
        self.t4_path = "../../resources/graphics/tiles/door.png"
        self.t5_path = "../../resources/graphics/tiles/heroTile.png"

        self.colors = ['BLUE', 'PURPLE', 'YELLOW', 'RED', 'WHITE', 'CYAN']

        self.scaling_factor = scaling_factor
        self.paint_step = int(100 * scaling_factor)

        #Add all the paths
        self.paths = []
        self.paths.append(root + "/floor.png")
        self.paths.append(root + "/wall.png")
        self.paths.append(root + "/treasure.png")
        self.paths.append(root + "/enemy.png")
        self.paths.append(root + "/door.png")
        self.paths.append(root + "/enemy.png")
        self.paths.append(root + "/heroTile.png")

        #Open all the raw images!
        self.raw_images = []
        for path in self.paths:
            self.raw_images.append(Image.open(path))

        #Resize all the paintable images
        self.paintable_tiles = []
        for raw_image in self.raw_images:
            self.paintable_tiles.append(raw_image.resize((int(raw_image.width * self.scaling_factor), int(raw_image.height * self.scaling_factor)), Image.ANTIALIAS))

        #Set all the colors
        self.colors = ['KHAKI', 'PURPLE', 'YELLOW', 'ORANGE', 'GREEN', 'RED', 'CYAN']

    def drawPixels(self, canvas, room, minimal=False):
        if(isinstance(room, Room)):
            return(self.__paintPixelsUsingRoom(canvas, room, minimal))
        elif(isinstance(room, np.ndarray)):
            return(self.__paintPixelsUsingMatrix(canvas, room, minimal))

    def drawTiles(self, canvas, room):
        if isinstance(room, Room):
            return self.__drawRoomUsingRoom(canvas, room)
        elif(isinstance(room, np.ndarray)):
            return self.__drawRoomUsingMatrix(canvas, room)

    #Expecting new empty image (or filled) and a np bidimensional array
    def __paintPixelsUsingMatrix(self, canvas, matrix, simple):
        draw =ImageDraw.Draw(canvas)
        self.paint_step = 1 if simple else int(100 * self.scaling_factor)
        for x in range(0, matrix.shape[1]):
            for y in range(0, matrix.shape[0]):
                draw.rectangle([(x * self.paint_step, y * self.paint_step), ((x+1) * self.paint_step, (y+1) * self.paint_step)],
                               fill=self.colors[matrix[y,x]],
                               outline=None)
        del draw
        self.paint_step = int(100 * self.scaling_factor)
        return canvas

    #Expecting new empty image (or filled) and a room
    def __paintPixelsUsingRoom(self, canvas, room, simple):
        draw =ImageDraw.Draw(canvas)
        self.paint_step = 1 if simple else int(100 * self.scaling_factor)
        for x in range(0, room.width):
            for y in range(0, room.height):
                draw.rectangle([(x * self.paint_step, y * self.paint_step), ((x+1) * self.paint_step, (y+1) * self.paint_step)],
                               fill=self.colors[room.getTile(x, y)],
                               outline=None)
        del draw
        self.paint_step = int(100 * self.scaling_factor)
        return canvas

    #Expecting new empty image (or filled) and a np bidimensional array (cannot do custom tiles)
    def __drawRoomUsingMatrix(self, canvas, matrix):
        for x in range(0, matrix.shape[1]):
            for y in range(0, matrix.shape[0]):
                canvas.paste(self.paintable_tiles[matrix[y,x]], (x * self.paint_step, y * self.paint_step))
        return canvas

    #Expecting new empty image (or filled) and a room
    def __drawRoomUsingRoom(self, canvas, room):
        for x in range(0, room.width):
            for y in range(0, room.height):
                canvas.paste(self.paintable_tiles[room.getTile(x, y)], (x * self.paint_step, y * self.paint_step))

        for custom_tile in room.custom_tiles:
            #             if custom_tile.tile == ENEMY_BOSS:
            boos_image = self.raw_images[5].resize((int(self.raw_images[5].width * 3),
                                                    int(self.raw_images[5].height * 3)),
                                                   Image.ANTIALIAS)
            canvas.paste(boos_image, ((custom_tile.x - 1) * self.paint_step, (custom_tile.y - 1) * self.paint_step))


        return canvas