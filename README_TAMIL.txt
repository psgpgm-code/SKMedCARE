SKMedCARE Auto Reminder Android Project — Ordered Upload Package

இந்த ZIP-ல் GitHub Repository-க்கு upload செய்ய வேண்டிய கோப்புகள் சரியான folder order-ல் உள்ளன.

ZIP-ஐ extract செய்த பிறகு repository ROOT-க்கு upload செய்ய வேண்டிய அமைப்பு:
- settings.gradle
- build.gradle
- gradle.properties
- app/
- .github/workflows/build-apk.yml

மொபைலில்:
1. GitHub-ல் உங்கள் SKMedCARE தனி repository திறக்கவும்.
2. ZIP-ஐ extract செய்யவும்.
3. ZIP-க்குள் இருக்கும் கோப்புகளை/folders-ஐ repository root-க்கு upload செய்யவும். ZIP file-ஐ மட்டும் repository-க்கு upload செய்ய வேண்டாம்.
4. `.github/workflows/build-apk.yml` path அப்படியே இருக்க வேண்டும்.
5. GitHub Actions tab > Build SKMedCARE Auto Reminder APK > Run workflow.
6. Workflow success ஆனதும் artifact `SKMedCARE-AutoReminder-APK`-ஐ download செய்யவும்.
7. Artifact ZIP-ஐ extract செய்து `app-debug.apk` install செய்யவும்.

கவனம்:
- இது source/build project ZIP; install செய்யும் APK அல்ல.
- இந்த project-ஐ SKMedKART Admin repository-ல் upload செய்ய வேண்டாம்.
- Build workflow-ல் Gradle setup step தெளிவாக சேர்க்கப்பட்டுள்ளது.
- Build வெற்றி அல்லது actual-device notification accuracy இங்கே உறுதி செய்யப்படவில்லை; Actions build log-ஐப் பார்க்க வேண்டும்.
- Android 13+ notification permission மற்றும் சில devices-ல் exact alarm/battery settings தேவைப்படலாம்.
- WhatsApp reminder message தயாராகத் திறக்கும்; WhatsApp தானாக message அனுப்பாது.
- Medicine dose/timing-ஐ prescription-ன்படி மட்டுமே உள்ளிடவும்.
