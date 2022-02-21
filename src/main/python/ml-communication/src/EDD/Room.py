from enum import Enum

import numpy as np


class TileData(Enum):
    FLOOR = 0
    WALL = 1
    TREASURE = 2
    ENEMY = 3
    DOOR = 4
    ENEMY_BOSS = 5
    HERO = 6


class CustomTiles():
    def __init__(self, x, y, value):
        self.x = x
        self.y = y
        self.tile = value


class Room:

    def __init__(self, room_id, width, height, customs=[]):
        self.room_id = room_id
        self.width = width
        self.height = height
        self.matrix = np.zeros([height, width], dtype=int)

        self.custom_tiles = customs
        self.test_data = []

    def setTilesFromMatrix(self, other_matrix):
        self.matrix = other_matrix

    def setTile(self, x, y, tile):
        self.matrix[y, x] = tile

    def getTile(self, x, y):
        return self.matrix[y, x]

    def set_data_info(self, Leniency, Linearity, Similarity, NMesoPatterns, NSpatialPatterns, Symmetry,
                      Inner_Similarity, Fitness,
                      Wall_Count, Wall_Density, Wall_Sparsity, Enemy_Count, Enemy_Density, Enemy_Sparsity,
                      Treasure_Count, Treasure_Density, Treasure_Sparsity,
                      Floor_Count, Floor_Density, Floor_Sparsity):

        # self.room_id = room_id
        self.leniency = Leniency
        self.linearity = Linearity
        self.similarity = Similarity
        self.n_meso_patt = NMesoPatterns
        self.n_spatial_patt = NSpatialPatterns
        self.symmetry = Symmetry
        self.inner_sim = Inner_Similarity
        self.fitness = Fitness
        self.wall_count = Wall_Count
        self.wall_density = Wall_Density
        self.wall_sparsity = Wall_Sparsity
        self.enemy_count = Enemy_Count
        self.enemy_density = Enemy_Density
        self.enemy_sparsity = Enemy_Sparsity
        self.treasure_count = Treasure_Count
        self.treasure_density = Treasure_Density
        self.treasure_sparsity = Treasure_Sparsity
        self.floor_count = Floor_Count
        self.floor_density = Floor_Density
        self.floor_sparsity = Floor_Sparsity
        self.total_count = self.wall_count + self.enemy_count + self.treasure_count + self.floor_count
        self.wall_count = self.wall_count / self.total_count
        self.enemy_count = self.enemy_count / self.total_count
        self.treasure_count = self.treasure_count / self.total_count
        self.floor_count = self.floor_count / self.total_count

    def addMinPixel(self, minPixelnmpy):
        self.minPixelnmpy = minPixelnmpy
        self.test_data = self.minPixelnmpy.reshape(-1, 70*130*3)

    def addDefaultImage(self, defaultnmpy):
        self.defaultnmpy = defaultnmpy

    def addPixelImage(self, pixelnmpy):
        self.pixelnmpy = pixelnmpy

    def setRoomSize(self, room_width, room_height):
        self.room_width = room_width
        self.room_height = room_height
        self.room_size = room_width * room_height
