[![Maven Central](https://img.shields.io/maven-central/v/com.cookieinformation/mobileconsents.svg?label=latest%20release)](https://search.maven.org/artifact/com.cookieinformation/mobileconsents)
# Mobile Consents
### Android SDK for easy user consent management.

## Integration  
To add SDK to your app add dependency in `build.gradle(.kts)` file:

Groovy DSL
```groovy
implementation "com.cookieinformation:mobileconsents:<latest_release>"
```  

Kotlin DSL
```kotlin
implementation("com.cookieinformation:mobileconsents:<latest_release>")
```
  
#### Error Handling
All exceptions thrown by SDK are wrapped by an `IOException` and passed to the `onFailure` method of a `CallListener` callback.
  
#### Async operations
All SDK's public methods are executed asynchronously, on background thread pool. You should rely on callbacks (`CallListener`) to handle
operation results. You can easily wrap those callbacks with Coroutines / LiveData / RxJava etc, if you use any of them. Note that if you want
to process result on the main thread (update UI etc.) you have to switch the thread yourself. Every method of the SDK returns a `Subscription`
object, which you can use to cancel the ongoing request (see the `Async operation cancelling` section).
  
#### Dependencies: 
SDK exposes [OkHttp](https://square.github.io/okhttp/) in its API. 
  
### Using the SDK:

#### Initializing

To instantiate SDK use `Builder` static method of `MobileConsentSdk` class:

#### Java:
```java
MobileConsentSdk sdk = MobileConsentSdk.Builder(context)
  .partnerUrl("https://example.com")
  .callFactory(new OkHttpClient())
  .build();
```

#### Kotlin:
```kotlin
val sdk = MobileConsentSdk.Builder(context)
   .partnerUrl("https://example.com")
   .callFactory(OkHttpClient())
   .build()
```
Note that you have to pass `Context` of your Application/Activity to the builder.
The `partnerUrl` parameter defines server where all consents choices will be sent. `callFactory` method is optional - if OkHttp's [Call.Factory](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-call/-factory/) isn't provided, SDK will instantiate it's own.

#### Getting consent solution

To fetch `ConsentSolution` from server, use `fetchConsentSolution` method:

ConsentSolution object structure:
```kotlin
// ConsentSolution object structure
public data class ConsentSolution(
  val consentItems: List<ConsentItem>,
  val consentSolutionId: UUID,
  val consentSolutionVersionId: UUID,
  val uiTexts: UiTexts
)

public data class ConsentItem(
  val consentItemId: UUID,
  val translations: List<ConsentTranslation>,
  val required: Boolean,
  val type: Type, // Setting, Info
)

public data class ConsentTranslation(
  val language: String,
  val longText: String,
  val shortText: String
)

public data class TextTranslation(
  val languageCode: String,
  val text: String
)

```

#### Java:
```java
sdk.fetchConsentSolution(
  consenSolutiontId, // UUID of consent solution
  new CallListener<ConsentSolution>() {
    @Override public void onSuccess(ConsentSolution result) {
      // do something with result
    }

    @Override public void onFailure(@NotNull IOException error) {
      // do something with error
    }
  }
);
```

#### Kotlin:
```kotlin
sdk.fetchConsentSolution(
  consentSolutionId = consentSolutionId, // UUID of consent solution
  listener = object : CallListener<ConsentSolution> {
    override fun onSuccess(result: ConsentSolution) {
        // do something with result
    }

    override fun onFailure(error: IOException) {
        // do something with error
    }
  }
)
```
After downloading a solution, you can show all consent items to the user and obtain their choices.

#### Sending consent to a server

SDK requires you to gather all consent choices in one `Consent` object. : 
```kotlin
public data class Consent(
  val consentSolutionId: UUID,
  val consentSolutionVersionId: UUID,
  val processingPurposes: List<ProcessingPurpose>,
  val customData: Map<String, String>,
)
```

Inside, you can pass a list of `ProcessingPurpose` - user consents:
```kotlin
public data class ProcessingPurpose(
  val consentItemId: UUID,
  val consentGiven: Boolean,
  val language: String
)
```

A `Consent` object can also store any additional info you want - in a form of a `Map<String, String>` map as a `customData` field.

To post `Consent` to a server, use `postConsent` method:

#### Java:
```java
sdk.postConsent(
  consent, // Consent object
  new CallListener<Unit>() {
    @Override public void onSuccess(Unit result) {
      // consent sent successfully
    }

    @Override public void onFailure(@NotNull IOException error) {
      // do something with error
    }
  }
); 
```

#### Kotlin:
```kotlin
sdk.postConsent(
  consent = consent, // Consent object
  listener = object : CallListener<Unit> {
    override fun onSuccess(result: Unit) {
        // consent sent successfully
    }

    override fun onFailure(error: IOException) {
        // do something with error
    }
  }
)
```
Once a request is successful, all consent choices are stored in SDK's internal storage, as a map of consent item IDs and booleans representing user choices.

#### Getting locally saved consent data

To read consent choices from SDK, use following methods:

To retrieve all consent choices, saved on device memory, use `getConsentChoices` method:
#### Java:
```java
sdk.getSavedConsents(
  new CallListener<Map<UUID, Boolean>>() {
    @Override public void onSuccess(@NotNull Map<UUID, Boolean> result) {
      // do something with result
    }

    @Override public void onFailure(@NotNull IOException error) {
      // do something with error
    }
  }
 );
```

#### Kotlin:
```kotlin
sdk.getSavedConsents(
  listener = object : CallListener<Map<UUID, Boolean>> {
    override fun onSuccess(result: Map<UUID, Boolean>) {
        // do something with result
    }

    override fun onFailure(error: IOException) {
        // do something with error
    }
  }
)
```
  
To retrieve specific consent choice, use `getConsentChoice` method and pass id of `ConsentItem`:
#### Java:
```java
sdk.getSavedConsent(
  consentItemId, // UUID of consent item 
  new CallListener<Boolean>() {
    @Override public void onSuccess(@NotNull Boolean result) {
      // do something with result
    }

    @Override public void onFailure(@NotNull IOException error) {
      // do something with error
    }
  }
 );    
```

#### Kotlin:
```kotlin
sdk.getSavedConsent(
  consentItemId = consentItemId, // UUID of consent item 
  listener = object : CallListener<Boolean> {
    override fun onSuccess(result: Boolean) {
        // do something with result
    }

    override fun onFailure(error: IOException) {
        // do something with error
    }
  }
)
```

#### Async operation cancelling

Use `Subscription` object returned from every SDKs method.
#### Java:
```java
Subscription subscription = sdk.getConsentChoice(
  consentItemId, // UUID of consent item 
  new CallListener<Boolean>() {
    @Override public void onSuccess(@NotNull Boolean result) {
      // do something with result
    }

    @Override public void onFailure(@NotNull IOException error) {
      // do something with error
    }
  }
 );   

subscription.cancel(); 
```

#### Kotlin:
```kotlin
val subscription = sdk.getConsentChoices(
  consentItemId = consentItemId, // UUID of consent item 
  listener = object : CallListener<Boolean> {
    override fun onSuccess(result: Boolean) {
        // do something with result
    }

    override fun onFailure(error: IOException) {
        // do something with error
    }
  }
)

subscription.cancel()
```

#### UI template texts
The response from the server `ConsentSolution` also includes suggested texts to be used in the user interface.
There are two screens considered:
 - **PrivacyPreferences**: The screen where the user accepts consents when using the application for the first time or registering an account.
 - **PrivacyCenter**: The screen where the user can change his consents, usually displayed in the application preferences.

```kotlin
public data class UiTexts(
  val privacyPreferencesTitle: List<TextTranslation>,
  val privacyPreferencesDescription: List<TextTranslation>,
  val privacyPreferencesTabLabel: List<TextTranslation>,

  val privacyCenterButton: List<TextTranslation>,
  val acceptAllButton: List<TextTranslation>,
  val rejectAllButton: List<TextTranslation>,
  val acceptSelectedButton: List<TextTranslation>,
  val savePreferencesButton: List<TextTranslation>,

  val privacyCenterTitle: List<TextTranslation>,

  val poweredByLabel: List<TextTranslation>,
  val consentPreferencesLabel: List<TextTranslation>
)
```
