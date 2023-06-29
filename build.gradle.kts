tasks.wrapper {
    // Use the binary-only distribution, not the complete distribution that includes sources and documentation.
    // If you navigate into a Gradle type the IDE will download sources on-the-fly like it would for any other
    // dependency, so there's no good reason to always pay the extra download cost of the complete distribution.
    // The Gradle build itself uses the binary-only distribution: https://github.com/gradle/gradle/blob/3a2eb57158b6f7105e715844c2c147e3c914d7f1/gradle/wrapper/gradle-wrapper.properties
    distributionType = Wrapper.DistributionType.BIN
}
