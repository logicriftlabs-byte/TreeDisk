import os

filepath = 'gradle/libs.versions.toml'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('[libraries]\n', 'generativeai = "0.9.0"\n\n[libraries]\n')
content = content.replace('[plugins]\n', 'google-generativeai = { group = "com.google.ai.client.generativeai", name = "generativeai", version.ref = "generativeai" }\n\n[plugins]\n')

with open(filepath, 'w') as f:
    f.write(content)
