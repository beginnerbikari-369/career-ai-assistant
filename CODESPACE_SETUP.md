# Career AI Assistant - GitHub Codespaces Setup

## 🚀 Quick Start with GitHub Codespaces

This project is configured to work seamlessly with GitHub Codespaces - no local installations required!

### Step 1: Push to GitHub
1. Create a new repository on GitHub
2. Push this code to your repository
3. Open the repository in your browser

### Step 2: Launch Codespace
1. Click the **"Code"** button on your GitHub repository
2. Select **"Codespaces"** tab  
3. Click **"Create codespace on main"**

### Step 3: Automatic Setup
The Codespace will automatically:
- ✅ Install Java 17
- ✅ Install Android SDK
- ✅ Install Gradle
- ✅ Configure development environment
- ✅ Install VS Code extensions for Android development

### Step 4: Build Your App
Once the Codespace loads:
```bash
# The project should be ready to build
./gradlew build

# For debug build
./gradlew assembleDebug
```

### Step 5: Connect Your Phone
1. **Enable Developer Options** on your Android phone:
   - Go to Settings → About Phone → Tap Build Number 7 times
   
2. **Enable USB Debugging**:
   - Go to Settings → Developer Options → Enable USB Debugging
   
3. **Connect via ADB over TCP** (since Codespace is remote):
   ```bash
   # On your phone, connect to same network and enable wireless debugging
   # Then in Codespace:
   adb connect YOUR_PHONE_IP:5555
   ```

### Step 6: Deploy to Phone
```bash
# Install on connected device
./gradlew installDebug

# Or generate APK to download
./gradlew assembleDebug
# APK will be in: app/build/outputs/apk/debug/
```

## 🔧 Configuration Files Included

- **Firebase**: `google-services.json` (update with your Firebase project)
- **API Keys**: `local.properties` (OpenAI key already configured)
- **Gradle**: All build configurations ready

## 📱 Features Ready to Test

Your Career AI Assistant includes:
- ✅ AI Chat with OpenAI integration
- ✅ Career goal tracking
- ✅ Habit management
- ✅ Google Calendar integration
- ✅ Secure data storage
- ✅ Background notifications
- ✅ Firebase sync

## 🆘 Need Help?

If you encounter any issues:
1. Check that your Firebase project is properly configured
2. Verify your OpenAI API key is valid
3. Ensure your phone has USB debugging enabled
4. Make sure you're connected to the same network for wireless ADB

## 💡 Benefits of This Setup

- **No local installations** required
- **Consistent environment** for all developers
- **Cloud-based** development with powerful VMs
- **Easy collaboration** and sharing
- **Automatic updates** and dependencies