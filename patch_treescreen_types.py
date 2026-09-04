import os

filepath = 'app/src/main/java/com/example/ui/TreeScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace(
"""                        onToggle = { directoryNode -> 
                            if (!directoryNode.isExpanded) {
                                // If we are expanding it, set it as the last expanded node
                                lastExpandedNode = directoryNode
                            } else if (lastExpandedNode == directoryNode) {
                                // If we are collapsing the currently selected one, clear it
                                lastExpandedNode = null
                            }
                            viewModel.toggleNode(directoryNode) 
                        },""",
"""                        onToggle = { nodeToToggle -> 
                            if (nodeToToggle is StorageNode.DirectoryNode) {
                                if (!nodeToToggle.isExpanded) {
                                    lastExpandedNode = nodeToToggle
                                } else if (lastExpandedNode == nodeToToggle) {
                                    lastExpandedNode = null
                                }
                                viewModel.toggleNode(nodeToToggle) 
                            }
                        },"""
)

with open(filepath, 'w') as f:
    f.write(content)
