# android-release-playground

A mostly empty app used to test our release process.

To build an unsigned release APK, run:

```shell
./gradlew app:asR -Psigning=unsigned
```

To build an release APK signed with the upload key pair, run:

```shell
./gradlew app:asR -Psigning=upload
```

To generate a new JKS file containing a 2048-bit RSA key pair, run:

```shell
keytool -genkeypair \
	-keystore debug.jks \
	-storetype jks \
	-storepass abcdef \
	-dname CN=Monzo \
	-keyalg rsa \
	-keysize 2048 \
	-alias monzo_debug \
	-keypass abcdef \
	-validity 36500
```
