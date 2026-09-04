import re

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'r') as f:
    content = f.read()

remote_section = """
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                RemoteConnectionsSection(
                                    connections = remoteConnections,
                                    onAddClick = { showAddRemoteDialog = true },
                                    onConnectionClick = { 
                                        viewModel.scanRemoteConnection(it)
                                    },
                                    onDeleteConnection = { viewModel.deleteRemoteConnection(it) }
                                )
                            }
"""

# We need to insert `remote_section` right before the end of the first LazyColumn
# Around line 538 is `} else { // Mobile Single-Pane Vertical Layout`
# Let's find:
#                                 }
#                             }
#                         }
#                     }
#                 } else {
#                     // Mobile Single-Pane Vertical Layout

target_1 = """                                }
                            }
                        }
                    }
                } else {"""
replacement_1 = """                                }
                            }
""" + remote_section + """                        }
                    }
                } else {"""

content = content.replace(target_1, replacement_1)

# For the mobile view:
target_2 = """                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable"""
replacement_2 = """                                }
                            }
""" + remote_section + """                        }
                    }
                }
            }
        }
    }
}

@Composable"""

content = content.replace(target_2, replacement_2)

with open('app/src/main/java/com/example/ui/DashboardScreen.kt', 'w') as f:
    f.write(content)
