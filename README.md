# fsm-edit
Finite state machine editor.

## Controls

* **Ctrl + Drag** - Create a new edge from one node to another.
* **Ctrl + Drag an edge** - Change its destination or drop on empty space to delete it.

## File Menu

The "File" menu provides options to create a new graph, open an existing one and save the current graph. Data is serialized using Java's built-in object serialization and uses the `.fsm` extension by default.
