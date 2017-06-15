#include <Python.h>
#include "shiny.h"

static PyMethodDef ShinyMethods[] = {
    {"shiny",  spam_system, METH_VARARGS,
     "Execute shiny function."},
    {NULL, NULL, 0, NULL}        /* Sentinel */
};

PyMODINIT_FUNC
initshiny(void)
{
    (void) Py_InitModule("shiny", SpamMethods);
}

static PyObject *
shiny_shiny(PyObject *self, PyObject *args)
{
    const char *command;
    int i;

    if (!PyArg_ParseTuple(args, "", &command))
        return NULL;
    i = shiny();
    return Py_BuildValue("i", i);
}
