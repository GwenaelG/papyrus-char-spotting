# -*- coding: utf-8 -*-
"""
Created on Thu Oct 15 14:29:19 2020

@author: Gwenael
"""

import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt
import os
import csv 
import random

# =============================================================================
# bounding box coordinates: 
#     x -> width, 1st coord
#     y -> height, 2nd coord
# 
# opencv image = matrix:
#     y -> row, 1st coord
#     x -> col, 2nd coord
# =============================================================================

# source image, original character
source_loc = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/chars/orig_bin_03_epsilon.png'
source_img = cv.imread(source_loc, 0)
source_h,source_w = source_img.shape

# target image, document
target_loc = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/pages/orig_bin_03.png'
target_img = cv.imread(target_loc, 0)
target_h, target_w = target_img.shape

# extract bounding boxes locations
groundtruth_loc = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/groundtruth/epsilon_gt_doc03.csv'
with open(groundtruth_loc) as gt_file:
    csv_reader = csv.reader(gt_file, delimiter=';')
    bounding_boxes_char = [[int(line[4]), int(line[5]), int(line[6]), int(line[7])] for line in csv_reader if len(line) == 8]

bb_location = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/boxes/bin_03/'
if not os.path.exists(bb_location):
    os.makedirs(bb_location)

# save bounding boxes images
for n,bb_char in enumerate(bounding_boxes_char):
    img_bb = target_img[bb_char[1]:bb_char[3],bb_char[0]:bb_char[2]]
    cv.imwrite(bb_location+'bb_char_'+str(n)+'.png', img_bb)
    
n_boxes = 100
bounding_boxes_other = []

for n in range(n_boxes):
    while True:
        x = random.randint(0, target_w - source_w - 1)
        y = random.randint(0, target_h - source_h - 1)
        in_bb = False
        for bb in bounding_boxes_char:
            if (x > bb[0] - source_w) and (x < bb[2]):
                if (y > bb[1] - source_h) and (x < bb[3]):
                    print("fail @"+str(n))
                    in_bb = True
                    break
        if not in_bb:
            bounding_boxes_other.append([x, y, x + source_w, y + source_h])
            break
        
for n, bb_other in enumerate(bounding_boxes_other):
    img_bb = target_img[bb_other[1]:bb_other[3], bb_other[0]:bb_other[2]]
    cv.imwrite(bb_location+'bb_other_'+str(n)+'.png', img_bb)
    
