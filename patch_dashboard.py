with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

# 1. Add AddRemoteConnectionDialog handling
dialog_code = """
    // Add Remote Connection Dialog
    if (showAddRemoteDialog) {
        AddRemoteConnectionDialog(
            onDismiss = { showAddRemoteDialog = false },
            onAdd = { 
                viewModel.addRemoteConnection(it)
                showAddRemoteDialog = false 
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
"""

content = content.replace("    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {", dialog_code)

# 2. Add RemoteConnections to tablet layout
# The tablet layout left column ends before `CupertinoSectionHeader("SYSTEM STATS")` in right column?
# It's probably easier to add it after "LARGEST FILES". Let's use grep to find the right spot.
