# TCP server example
import struct
import socket
import threading
import numpy as np
import cv2
from detector import DetectorAPI

def loadImage(connection):
	while True:
		buf = bytes()
		data = bytes()
		connection.settimeout(1)
		while len(buf)<4:
			buf += connection.recv(4-len(buf), socket.MSG_WAITALL)
		size = struct.unpack('!i', buf)[0]
		print ("receiving %s bytes" % size)
		
		while len(data)<size:
			data += connection.recv(1024, socket.MSG_WAITALL)
			
		nparr = np.frombuffer(data, dtype=np.uint8)
		img = cv2.imdecode(nparr, -1)
		return img

def processImage(img):
	detection = False
	boxes, scores, classes, num = odapi.processFrame(img)
	# Visualization of the results of a detection.
	for i in range(len(boxes)):
		# Class 1 represents human
		if classes[i] == 1 and scores[i] > threshold:
			box = boxes[i]
			cv2.rectangle(img,(box[1],box[0]),(box[3],box[2]),(255,0,0),2)
			detection = True
	return detection

class ConnectionHandler(threading.Thread):
	"""objet thread gérant la réception des messages"""
	def __init__(self, conn):
		threading.Thread.__init__(self)
		self.connection = conn

	def run(self):
		try:
			print("I got a connection from ", address)
			img = loadImage(self.connection)
			if(processImage(img)):
				buffer = cv2.imencode('.jpg', img)[1]
				img_bytes = buffer.tobytes()
				length = len(img_bytes)
				payload = length.to_bytes(4, 'big')
				payload += img_bytes[0:length]
				self.connection.send(payload)
			else:
				zero = 0
				self.connection.send(bytes(zero.to_bytes(4, 'big')))
		except Exception as e:
			print(e)
		finally:
			self.connection.close()

if __name__ == "__main__":
	model_path = '/home/charles/workspaces/camm/camm/hr/frozen_inference_graph.pb'
	odapi = DetectorAPI(path_to_ckpt=model_path)
	threshold = 0.5
	server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	server_socket.bind(("", 5000))
	server_socket.listen(5)
	print("TCPServer Waiting for client on port 5000")
	while True:
		connection = None
		try:
			connection, address = server_socket.accept()
			ConnectionHandler(connection).start()
		except:
			if(connection != None):
				connection.close()
