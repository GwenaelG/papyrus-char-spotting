# -*- coding: utf-8 -*-
"""
Created on Tue Nov  3 09:50:16 2020

@author: Gwenael
"""

import cv2 as cv

def binarize(orig_img, dest_img):
    img = cv.imread(orig_img, 0)
    t, img = cv.threshold(img, 250, 255, cv.THRESH_BINARY)
    cv.imwrite(dest_img, img)
    
    
    
orig_img = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/better_pages/doc_02_reconstructed_bold.png'
dest_img = 'C:/Users/Gwenael/Desktop/MT/papyrus-char-spotting/files/original_bin/better_pages/bin_02_reconstructed_bold.png'
binarize(orig_img, dest_img)

