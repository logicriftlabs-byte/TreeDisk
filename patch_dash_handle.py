import re

filepath = 'app/src/main/java/com/example/ui/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

imports = """import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp"""
content = content.replace("import androidx.compose.ui.Modifier\nimport androidx.compose.foundation.Canvas\nimport androidx.compose.ui.graphics.drawscope.Fill", imports)

# Remove the root pointerInput and change it to a Box
old_root = """    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount > 20) {
                        onClose()
                    }
                }
            }
    ) {"""

new_root = """    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {"""
content = content.replace(old_root, new_root)

old_handle = """        // Handle
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.width(40.dp).height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Pull down to close", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }"""

new_handle = """        // Handle space
        Spacer(modifier = Modifier.height(80.dp))
    }
    
    // Bottom 10% interactive area to pull up and close
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.12f)
            .align(Alignment.BottomCenter)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -15) { // Pull up
                        onClose()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Close",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    }"""

content = content.replace(old_handle, new_handle)

with open(filepath, 'w') as f:
    f.write(content)

