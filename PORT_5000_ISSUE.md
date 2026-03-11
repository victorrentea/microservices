## Setup Requires Free Port 5000

The K8s setup script requires port 5000 to be available for the local Docker registry.

### Current Status

**Port 5000 is currently in use** - this is typically from:
- Docker Desktop's ControlCenter process
- A previously running registry
- Another service

### Solutions

#### Option 1: Quick Fix (Recommended)
Kill the process holding port 5000:
```bash
lsof -i :5000      # Find what's using port 5000
kill -9 <PID>      # Kill it
```

Then try setup again:
```bash
./setup.sh
```

#### Option 2: Use Different Port
Modify the setup script to use a different port:
1. Edit `setup.sh`
2. Change `REGISTRY_PORT=5000` to `REGISTRY_PORT=5001`
3. Update all port references
4. Run setup

#### Option 3: Restart Docker
```bash
# Quit Docker Desktop
# Wait 30 seconds
# Relaunch Docker Desktop
# Try setup again
```

#### Option 4: Check What's Using Port 5000
```bash
lsof -i :5000
netstat -an | grep 5000  # Alternative command
```

If it's Docker Desktop or a hung process, kill it:
```bash
kill -9 <PID>
```

### Try Setup Again

Once port 5000 is free:
```bash
cd /Users/victorrentea/workspace/microservices/k8s
./setup.sh
```

The setup script should now work and display:
- Prerequisites check ✓
- Registry setup ✓
- Cluster creation... (takes ~3-5 minutes)
- Image building... (takes ~15-20 minutes first time)
- Services deployment... (takes ~2-3 minutes)

**Total time: 25-35 minutes first run**

