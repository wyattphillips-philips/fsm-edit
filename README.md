# fsm-edit
Finite state machine editor.

## Controls

* **Ctrl + Drag** - Create a new edge from one node to another.
* **Ctrl + Drag an edge** - Change its destination or drop on empty space to delete it.
* **Drag on empty space** - Box select multiple nodes.
* **Drag a selected node** - Move all selected nodes together.
* **Right-click** - Access context menu with options to add, delete,
  copy or paste nodes. Pasted nodes appear relative to the cursor.
* **Ctrl+C / Ctrl+V** - Copy and paste nodes using the keyboard. Paste
  centers the nodes at the current cursor position.
* **Delete** - Remove the currently selected node(s).

## File Menu

The "File" menu provides options to create a new graph, open an existing one and save the current graph. Data is serialized using Java's built-in object serialization and uses the `.fsm` extension by default.

## Node Properties

The node properties panel includes a **Lock Position** checkbox. When checked,
the selected node cannot be dragged or repositioned via its X and Y fields.
When multiple nodes are selected, the panel shows how many nodes are selected, e.g., "10 Nodes Selected".
The color picker remembers previously chosen swatches so you can quickly reuse
recent colors when editing multiple nodes.

## Edge Properties

When an edge is selected the properties panel lets you change its spline type,
curvature and an optional text string. The text is drawn near the middle of the
edge in the graph view and is rendered over a white background so it remains
readable regardless of the edge color.
