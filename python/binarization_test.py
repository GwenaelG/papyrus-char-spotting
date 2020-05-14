# -*- coding: utf-8 -*-
"""
Created on Tue Mar 10 14:15:38 2020

@author: Gwenael
"""

# =============================================================================
# REMEMBER
# 
# 0 is blackest black
# 255 is whitest white
# 
# cv.threshold(img, thresh, max, cv.THRESH_BINARY) takes everything over thresh 
# and sets it to max, everything under 150 is set to 0
# i.e. higher threshold means more black pixels
# =============================================================================


import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt
import os

def DoG(img, s1, s2):
    blur1 = cv.GaussianBlur(img, (0,0), s1)
    blur2 = cv.GaussianBlur(img, (0,0), s2)
    out = cv.add(blur1,-blur2)
    return out

def mb_bin_comp(i, patch_only = False):
    # 3 median blurs with each 6 binarization THRESHOLDS on the patch
    img = cv.imread('C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/gray_'+DOCUMENTS[i]+'.jpg',0)
    if patch_only:
        img = img[PATCHES[i][0]:PATCHES[i][0]+500, PATCHES[i][1]:PATCHES[i][1]+500]
    for j,ker in enumerate(KER_SIZES):
        img_mb = cv.medianBlur(img, ker)
        plt.subplot(len(KER_SIZES),len(THRESHOLDS)+1, 7*j + 1),plt.imshow(img_mb, 'gray')
        plt.title(DOCUMENTS[i]+': median blur '+str(ker)),plt.xticks([]),plt.yticks([])
        for k,thr in enumerate(THRESHOLDS):
            t, img_mb_bin = cv.threshold(img_mb, thr, 255, cv.THRESH_BINARY)
            plt.subplot(len(ker),len(THRESHOLDS)+1, 7*j + k + 2),plt.imshow(img_mb_bin, 'gray')
            plt.title(DOCUMENTS[i]+': median blur '+str(ker)+' + bin '+str(thr)),plt.xticks([]), plt.yticks([])
    plt.show()

def DoG_comp(i, patch_only = False):
    #4 different values for each sigma1 and sigma2 
    img = cv.imread('C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/gray_'+DOCUMENTS[i]+'.jpg',0)
    if patch_only:
        img = img[PATCHES[i][0]:PATCHES[i][0]+500, PATCHES[i][1]:PATCHES[i][1]+500]
    newpath = 'C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/DoG_comp/'
    if not os.path.exists(newpath):
        os.makedirs(newpath)
    for s1 in SIGMAS1:
        for s2 in SIGMA2:
            img_dog = DoG(img, s1, s2)
            if not patch_only:
                plt.imsave(newpath+DOCUMENTS[i]+'_DoG_'+str(s1)+'_'+str(s2)+'.png', img_dog, cmap='gray')
            else:
                plt.imsave(newpath+DOCUMENTS[i]+'_patch_DoG_'+str(s1)+'_'+str(s2)+'.png', img_dog, cmap='gray')

def DoG_bin_comp(i, patch_only = False):
    img = cv.imread('C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/gray_'+DOCUMENTS[i]+'.jpg',0)
    if patch_only:
        img = img[PATCHES[i][0]:PATCHES[i][0]+500, PATCHES[i][1]:PATCHES[i][1]+500]
    newpath = 'C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/DoG_bin_comp/'
    if not os.path.exists(newpath):
        os.makedirs(newpath)
    img_dog = DoG(img, BEST_SIGMA1, BEST_SIGMA2)
    for thr in THRESHOLDS:
        t, img_dog_bin = cv.threshold(img_dog, thr, 255, cv.THRESH_BINARY)
        if not patch_only:
            plt.imsave(newpath+DOCUMENTS[i]+'_DoG_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'_bin_'+str(thr)+'.png', img_dog_bin, cmap='gray')
        else:
            plt.imsave(newpath+DOCUMENTS[i]+'_patch_DoG_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'_bin_'+str(thr)+'.png', img_dog_bin, cmap='gray')
                            
def DoG_mb_comb_comp(i, patch_only = False):
    img = cv.imread('C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/gray_'+DOCUMENTS[i]+'.jpg',0)
    if patch_only:
        img = img[PATCHES[i][0]:PATCHES[i][0]+500, PATCHES[i][1]:PATCHES[i][1]+500]
    newpath = 'C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/DoG_mb_comb_comp/'
    if not os.path.exists(newpath):
        os.makedirs(newpath)
    ### DoG_MB ###
    # it means first DoG then mb, ie mb(DoG(img))
    for s2 in SIGMA2:
        img_dog = DoG(img, BEST_SIGMA1, s2)
        for ker in KER_SIZES:
            img_dog_mb = cv.medianBlur(img_dog, ker)
            if not patch_only:
                plt.imsave(newpath+DOCUMENTS[i]+'_DoG_'+str(BEST_SIGMA1)+'_'+str(s2)+'_mb_'+str(ker)+'.png', img_dog_mb, cmap='gray')
            else:
                plt.imsave(newpath+DOCUMENTS[i]+'_patch_DoG_'+str(BEST_SIGMA1)+'_'+str(s2)+'_mb_'+str(ker)+'.png', img_dog_mb, cmap='gray')
    ### MB DoG ### 
    # ie DoG(mb(img))
    for ker in KER_SIZES:
        img_mb = cv.medianBlur(img, ker)
        for s2 in SIGMA2:
            img_mb_dog = DoG(img_mb, BEST_SIGMA1, s2)
            if not patch_only:
                plt.imsave(newpath+DOCUMENTS[i]+'_mb_'+str(ker)+'_DoG_'+str(BEST_SIGMA1)+'_'+str(s2)+'.png', img_mb_dog, cmap='gray')
            else:
                plt.imsave(newpath+DOCUMENTS[i]+'_patch_mb_'+str(ker)+'_DoG_'+str(BEST_SIGMA1)+'_'+str(s2)+'.png', img_mb_dog, cmap='gray')
    
def DoG_mb_bin_comp(i, thr_list, patch_only = False):
    img = cv.imread('C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/gray_'+DOCUMENTS[i]+'.jpg',0)
    if patch_only:
        img = img[PATCHES[i][0]:PATCHES[i][0]+500, PATCHES[i][1]:PATCHES[i][1]+500]
    newpath = 'C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/DoG_mb_bin_comp/'
    if not os.path.exists(newpath):
        os.makedirs(newpath)
    img_dog = DoG(img, BEST_SIGMA1, BEST_SIGMA2)
    img_dog_mb = cv.medianBlur(img_dog, BEST_KER_SIZE[i])
    img_mb = cv.medianBlur(img, BEST_KER_SIZE[i])
    img_mb_dog = DoG(img_mb, BEST_SIGMA1, BEST_SIGMA2)
    for thr in thr_list:
        t, img_dog_mb_bin = cv.threshold(img_dog_mb, thr, 255, cv.THRESH_BINARY)
        t, img_mb_dog_bin = cv.threshold(img_mb_dog, thr, 255, cv.THRESH_BINARY)
        if not patch_only:
            plt.imsave(newpath+DOCUMENTS[i]+'_DoG_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'_mb_'+str(BEST_KER_SIZE[i])+'_bin_'+str(thr)+'.png', img_dog_mb_bin, cmap='gray')
            plt.imsave(newpath+DOCUMENTS[i]+'_mb_'+str(BEST_KER_SIZE[i])+'_DoG_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'_bin_'+str(thr)+'.png', img_mb_dog_bin, cmap='gray')
        else:
            plt.imsave(newpath+DOCUMENTS[i]+'_patch_DoG_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'_mb_'+str(BEST_KER_SIZE[i])+'_bin_'+str(thr)+'.png', img_dog_mb_bin, cmap='gray')
            plt.imsave(newpath+DOCUMENTS[i]+'_patch_mb_'+str(BEST_KER_SIZE[i])+'_DoG_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'_bin_'+str(thr)+'.png', img_mb_dog_bin, cmap='gray')
    
def adapt_bin_comp(i, patch_only = False):
    img = cv.imread('C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/gray_'+DOCUMENTS[i]+'.jpg',0)
    if patch_only:
        img = img[PATCHES[i][0]:PATCHES[i][0]+500, PATCHES[i][1]:PATCHES[i][1]+500]
    newpath = 'C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/adapt_bin_comp/'
    if not os.path.exists(newpath):
        os.makedirs(newpath)
    #mb
    img_mb = cv.medianBlur(img, BEST_KER_SIZE[i])
    img_mb_mean_bin = cv.adaptiveThreshold(img_mb, 255, cv.ADAPTIVE_THRESH_MEAN_C, cv.THRESH_BINARY, MEAN_NBHD, MEAN_C)
    img_mb_gauss_bin = cv.adaptiveThreshold(img_mb, 255, cv.ADAPTIVE_THRESH_GAUSSIAN_C, cv.THRESH_BINARY, GAUSS_NBHD, GAUSS_C)
    #dog
    img_dog = DoG(img, BEST_SIGMA1, BEST_SIGMA2)
    img_dog_mean_bin = cv.adaptiveThreshold(img_dog, 255, cv.ADAPTIVE_THRESH_MEAN_C, cv.THRESH_BINARY, MEAN_NBHD, MEAN_C)
    img_dog_gauss_bin = cv.adaptiveThreshold(img_dog, 255, cv.ADAPTIVE_THRESH_GAUSSIAN_C, cv.THRESH_BINARY, GAUSS_NBHD, GAUSS_C)
    #mb_dog
    img_mb_dog = DoG(img_mb, BEST_SIGMA1, BEST_SIGMA2)
    img_mb_dog_mean_bin = cv.adaptiveThreshold(img_mb_dog, 255, cv.ADAPTIVE_THRESH_MEAN_C, cv.THRESH_BINARY, MEAN_NBHD, MEAN_C)
    img_mb_dog_gauss_bin = cv.adaptiveThreshold(img_mb_dog, 255, cv.ADAPTIVE_THRESH_GAUSSIAN_C, cv.THRESH_BINARY, GAUSS_NBHD, GAUSS_C)
    if not patch_only:
        plt.imsave(newpath+DOCUMENTS[i]+'_mean_bin_'+str(MEAN_NBHD)+'_'+str(MEAN_C)+'_mb_'+str(BEST_KER_SIZE[i])+'.jpg',img_mb_mean_bin, cmap='gray')
        plt.imsave(newpath+DOCUMENTS[i]+'_gauss_bin_'+str(GAUSS_NBHD)+'_'+str(GAUSS_C)+'_mb_'+str(BEST_KER_SIZE[i])+'.jpg',img_mb_gauss_bin, cmap='gray')
        plt.imsave(newpath+DOCUMENTS[i]+'_mean_bin_'+str(MEAN_NBHD)+'_'+str(MEAN_C)+'_dog_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'.jpg',img_dog_mean_bin, cmap='gray')
        plt.imsave(newpath+DOCUMENTS[i]+'_gauss_bin_'+str(GAUSS_NBHD)+'_'+str(GAUSS_C)+'_dog_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'.jpg',img_dog_gauss_bin, cmap='gray')
        plt.imsave(newpath+DOCUMENTS[i]+'_mean_bin_'+str(MEAN_NBHD)+'_'+str(MEAN_C)+'_mb_'+str(BEST_KER_SIZE[i])+'_dog_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'.jpg',img_mb_dog_mean_bin, cmap='gray')
        plt.imsave(newpath+DOCUMENTS[i]+'_gauss_bin_'+str(GAUSS_NBHD)+'_'+str(GAUSS_C)+'_mb_'+str(BEST_KER_SIZE[i])+'_dog_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'.jpg',img_mb_dog_gauss_bin, cmap='gray')
    else:
        plt.imsave(newpath+DOCUMENTS[i]+'_patch_mean_bin_'+str(MEAN_NBHD)+'_'+str(MEAN_C)+'_mb_'+str(BEST_KER_SIZE[i])+'.jpg',img_mb_mean_bin, cmap='gray') 
        plt.imsave(newpath+DOCUMENTS[i]+'_patch_gauss_bin_'+str(GAUSS_NBHD)+'_'+str(GAUSS_C)+'_mb_'+str(BEST_KER_SIZE[i])+'.jpg',img_mb_gauss_bin, cmap='gray')       
        plt.imsave(newpath+DOCUMENTS[i]+'_patch_mean_bin_'+str(MEAN_NBHD)+'_'+str(MEAN_C)+'_dog_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'.jpg',img_dog_mean_bin, cmap='gray')
        plt.imsave(newpath+DOCUMENTS[i]+'_patch_gauss_bin_'+str(GAUSS_NBHD)+'_'+str(GAUSS_C)+'_dog_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'.jpg',img_dog_gauss_bin, cmap='gray')
        plt.imsave(newpath+DOCUMENTS[i]+'_patch_mean_bin_'+str(MEAN_NBHD)+'_'+str(MEAN_C)+'_mb_'+str(BEST_KER_SIZE[i])+'_dog_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'.jpg',img_mb_dog_mean_bin, cmap='gray')
        plt.imsave(newpath+DOCUMENTS[i]+'_patch_gauss_bin_'+str(GAUSS_NBHD)+'_'+str(GAUSS_C)+'_mb_'+str(BEST_KER_SIZE[i])+'_dog_'+str(BEST_SIGMA1)+'_'+str(BEST_SIGMA2)+'.jpg',img_mb_dog_gauss_bin, cmap='gray')



DOCUMENTS = ['doc01', 'doc02', 'doc03', 'doc04', 'doc05', 'doc06', 'doc07', 'doc08', 'doc09', 'doc10' ]
PATCHES = [[1000, 900], [200, 500], [200, 650], [900, 200], [250, 50], [500, 300], [50, 50], [200, 400], [900, 300], [600, 100]]
THRESHOLDS = range(10,251,10) #between 70 and 100 for mb, 220+ for DoG


KER_SIZES = [5, 7, 9] 
BEST_KER_SIZE = [9, 9, 5, 9, 7, 7, 5, 5, 7, 9] #visual choice on median blur bin, seems to depend on char size/thickness

SIGMAS1 = [0.1, 0.5, 1]
BEST_SIGMA1 = 0.5 #not much visual difference between 0.1, 0.5 and 1; 2 was consistently too blurry - chosen on DoG comp
SIGMA2 = [10, 40] #10 seems to often be better (40 leads to large black markings on papyrus' border) - however doc02 and doc10 may prefer 40 (runs with both sigmas) - 100 way too big
BEST_SIGMA2 = 40 #chosen on DoG & DoG/mb combination

MEAN_NBHD = 11
MEAN_C = 2
GAUSS_NBHD = 11
GAUSS_C = 2
    
font = {'size': 20}

i=4
img = cv.imread('C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/gray_'+DOCUMENTS[i]+'.jpg',0)
img = cv.medianBlur(img, 5)
t, img = cv.threshold(img, 80, 255, cv.THRESH_BINARY)
plt.imsave('C:/Users/Gwenael/Desktop/MT/papyrus/Dibco/copies pour test/'+DOCUMENTS[i]+'/'+DOCUMENTS[i]+'mb_5_bin80.jpg', img, cmap='gray')




 