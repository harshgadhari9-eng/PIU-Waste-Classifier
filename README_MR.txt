PIU Waste Classifier - Android Studio Project

हे Android app project आहे. यात फोटो निवडणे, 224x224 RGB preprocessing,
TensorFlow Lite prediction आणि 9 waste categories साठी recycling advice आहे.

महत्त्वाचे:
- तुमच्या upload केलेल्या APP.py मध्ये Windows Tkinter UI आणि TensorFlow H5 model loader आहे.
- सध्याच्या upload मध्ये फक्त APP.py आहे; trained model (.h5), Models folder आणि Assets folder उपलब्ध नाहीत.
- त्यामुळे खरे AI prediction चालवण्यासाठी मूळ trained model आवश्यक आहे.
- त्या model चे TensorFlow Lite (.tflite) मध्ये conversion करून
  app/src/main/assets/waste_classifier.tflite या नावाने ठेवा.
- Input: float32 [1,224,224,3], RGB /255. Output: 9 scores, मूळ APP.py मधील CLASS_NAMES च्या क्रमाने.

APK बनवण्यासाठी:
1. Android Studio install करा.
2. Open करून हा पूर्ण project folder निवडा.
3. Gradle sync पूर्ण होऊ द्या.
4. वर Build > Build Bundle(s) / APK(s) > Build APK(s) निवडा.
5. APK app/build/outputs/apk/debug/app-debug.apk येथे तयार होईल.

टीप: सध्याच्या environment मध्ये Android SDK/Gradle build setup नाही, म्हणून इथे थेट install करण्याजोगा APK compile केलेला नाही. हा Android Studio project आहे; trained model जोडल्यानंतर build करता येईल.
