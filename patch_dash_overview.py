import re

filepath = 'app/src/main/java/com/example/ui/DashboardScreen.kt'
with open(filepath, 'r') as f:
    content = f.read()

# Make sure we have the necessary imports for Canvas and Math
if 'import androidx.compose.foundation.Canvas' not in content:
    content = content.replace('import androidx.compose.ui.Modifier', 'import androidx.compose.ui.Modifier\nimport androidx.compose.foundation.Canvas\nimport androidx.compose.ui.graphics.drawscope.Fill')

# Fetch the category stats and remote connections
content = content.replace(
'''    val freeSpace by viewModel.freeSpace.collectAsState()''',
'''    val freeSpace by viewModel.freeSpace.collectAsState()
    val categoryStats by viewModel.categoryStats.collectAsState()
    val remoteConnections by viewModel.remoteConnections.collectAsState()'''
)

# Fix Dashboard name
content = content.replace('Text("NucleusFS Dashboard",', 'Text("Overview",')

# Fix ACTIVE NODES stats
content = content.replace(
'''DashboardSummaryCard("ACTIVE NODES", "4 online", "2 local · 2 cloud", Modifier.weight(1f))''',
'''DashboardSummaryCard("ACTIVE NODES", "${1 + remoteConnections.size} online", "1 local · ${remoteConnections.size} cloud", Modifier.weight(1f))'''
)

# Update the graph UI to use Pie Chart
pie_chart_ui = """                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Canvas Pie Chart
                            Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val totalStatSize = categoryStats.sumOf { it.size }.toFloat()
                                    var startAngle = -90f
                                    if (totalStatSize == 0f) {
                                        drawArc(
                                            color = Color.DarkGray,
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = true
                                        )
                                    } else {
                                        categoryStats.forEach { stat ->
                                            val sweepAngle = (stat.size.toFloat() / totalStatSize) * 360f
                                            if (sweepAngle > 0) {
                                                drawArc(
                                                    color = stat.category.color,
                                                    startAngle = startAngle,
                                                    sweepAngle = sweepAngle,
                                                    useCenter = true,
                                                    style = Fill
                                                )
                                                startAngle += sweepAngle
                                            }
                                        }
                                    }
                                }
                                // Center hole for donut effect
                                Box(modifier = Modifier.size(50.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.15f)), contentAlignment = Alignment.Center) {
                                    Text("$percent%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            
                            // Legend
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                categoryStats.sortedByDescending { it.size }.take(4).forEach { stat ->
                                    val statGb = "%.2f".format(stat.size / (1024f * 1024f * 1024f))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(stat.category.color))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(stat.category.displayName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("$statGb GB", fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                
                                if (categoryStats.isEmpty()) {
                                    Text("No files analyzed yet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }"""

# Need to find the old graph UI and replace it
# The old code is:
old_graph_ui = """                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Circle placeholder
                            Box(modifier = Modifier.size(80.dp).clip(androidx.compose.foundation.shape.CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)), contentAlignment = Alignment.Center) {
                                Text("$percent%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            }
                            Spacer(modifier = Modifier.width(24.dp))
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Used", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$usedGb GB", fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Available", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$freeGb GB", fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("$totalGb GB", fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }"""

content = content.replace(old_graph_ui, pie_chart_ui)

with open(filepath, 'w') as f:
    f.write(content)

