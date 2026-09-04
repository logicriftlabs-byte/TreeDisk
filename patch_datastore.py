import os

filepath = 'app/src/main/java/com/example/data/RemoteConnectionDataStore.kt'
with open(filepath, 'r') as f:
    content = f.read()

# For parseConnections (decrypt)
old_parse = 'password = jsonObj.optString("password", ""),'
new_parse = 'password = SecurityUtils.decrypt(jsonObj.optString("password", "")),'
content = content.replace(old_parse, new_parse)

# For serializeConnections (encrypt)
old_serialize = 'put("password", conn.password)'
new_serialize = 'put("password", SecurityUtils.encrypt(conn.password))'
content = content.replace(old_serialize, new_serialize)

with open(filepath, 'w') as f:
    f.write(content)
