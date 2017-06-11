import sys
import ctypes
import os
#
root_path = os.path.dirname(__file__)
shiny = ctypes.CDLL(
	os.path.join(root_path,
    "external/lib/libshiny.so"))

from PyQt5.QtWidgets import QApplication, QWidget, QPushButton, QMessageBox
from PyQt5.QtGui import QIcon
from PyQt5.QtCore import pyqtSlot

class App(QWidget):

    def __init__(self):
        super().__init__()
        self.title = 'Degasolv Demo'
        self.left = 10
        self.top = 10
        self.width = 320
        self.height = 200
        self.initUI()

    def initUI(self):
        self.setWindowTitle(self.title)
        self.setGeometry(self.left, self.top, self.width, self.height)

        buttonReply = \
        QMessageBox.about(self,
            'Degasolv Demo',
            'Shiny method yields {shiny}'.format(shiny=shiny.shiny()))
        self.show()

if __name__ == '__main__':
    app = QApplication(sys.argv)
    ex = App()
    sys.exit(app.exec_())

