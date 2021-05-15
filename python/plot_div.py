# -*- coding: utf-8 -*-
"""
Created on Fri Jan  8 10:37:07 2021

@author: Gwenael
"""

import numpy as np
import cv2 as cv
import matplotlib.pyplot as plt
import glob

x1 = np.array([15, 118, 469, 1328, 4240, 7052, 8097])
y1 = np.array([0.0086, 0.3256, 4.5686, 36.712, 421.69, 1015.2, 1284.7])
x2 = np.array([15, 95, 380, 1083, 3529, 5847, 6784])
y2 = np.array([0.0063, 0.0155, 0.0296, 0.0902, 0.320, 0.460, 0.770])


plt.plot(x1,y1,'o:b', label='Keypoint')x
plt.plot(x2,y2,'s:r', label='Contour')
plt.title(' ')
plt.xlabel('Nodes')
plt.ylabel('Time (s)')
plt.legend(frameon=False, loc=5)


# x = np.arange(1000, 10001, 1000)
# y = np.array([1463, 16792, 57358, 178408, 442565, 808313, 1484170, 2237259, 3996883, 6360825])
# z = x ** 2

# plt.subplot(1,2,1)
# plt.plot(x,y/1000,'.-')
# plt.title('Time for graph parsing')
# plt.xlabel('Amount of nodes')
# plt.ylabel('time (s)')

# plt.subplot(1,2,2)
# plt.loglog(x,y/1000,'.-')
# plt.title('Time for graph parsing (log-log)')
# plt.xlabel('Amount of nodes')
# plt.ylabel('time (s)')


plt.show()
#plt.savefig('C:/Users/Gwenael/Desktop/MT/latex/graph_parsing.png', bbox_inches = 'tight')