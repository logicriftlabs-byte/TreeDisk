import os

filepath = 'app/src/main/java/com/example/ui/SettingsScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

if 'onBack: () -> Unit' not in content:
    content = content.replace('fun SettingsScreen(viewModel: StorageViewModel)', 'fun SettingsScreen(viewModel: StorageViewModel, onBack: () -> Unit = {})')
    
    # Add a back button in the header if there isn't one. It's inside a LazyColumn probably.
    content = content.replace(
        '''Text(
                        text = "Preferences",
                        fontSize = 28.sp,''',
        '''Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                        Text(
                            text = "Preferences",
                            fontSize = 28.sp,'''
    ).replace('color = MaterialTheme.colorScheme.onBackground\n                    )', 'color = MaterialTheme.colorScheme.onBackground\n                    )\n                    }')
    content = content.replace('import androidx.compose.material.icons.filled.Info', 'import androidx.compose.material.icons.filled.Info\nimport androidx.compose.material.icons.filled.ArrowBack')

with open(filepath, 'w') as f:
    f.write(content)
