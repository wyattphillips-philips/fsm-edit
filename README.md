# fsm-edit
Finite state machine editor.

## Controls

* **Ctrl + Drag** - Create a new edge from one node to another.
* **Ctrl + Drag an edge** - Change its destination or drop on empty space to delete it.
* **Drag on empty space** - Box select multiple nodes.
* **Drag a selected node** - Move all selected nodes together.

## File Menu

The "File" menu provides options to create a new graph, open an existing one and save the current graph. Data is serialized using Java's built-in object serialization and uses the `.fsm` extension by default.

## Node Properties

The node properties panel includes a **Lock Position** checkbox. When checked,
the selected node cannot be dragged or repositioned via its X and Y fields.
When multiple nodes are selected, the panel simply displays "Multiple Items Selected".
