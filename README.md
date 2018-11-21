# Pachatary Android App
This repo contains the Android application
code for Pachatary project.
This simple app aims to be a reference
to discover interesting places
and also to record our past experiences,
sharing content with other people.

Application domain is composed by `scenes`,
which are defined as something that happened
or can be done/seen in a located place.
A group of `scenes` are defined as an `experience`.

There are also `profiles`,
which are the representation of a person inside the app.

## App screens

Here is a quick documentation about the screens that compose the app:

#### WelcomeActivity

![WelcomeActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/welcome-android-screenshot.png)

This is the first activity that appears to a person that is not authenticated.
By clicking start new adventure, anonymous person is registered and guided to MainActivity.
I have an account button leads the person to LoginActivity.


#### LoginActivity

![LoginActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/login-android-screenshot.png)

Form to get an email with a login link.


#### MainActivity

This is the screen that appears when you open the app.
There are three section tabs: mine, explore and saved.

##### ExploreExperiencesTab

![ExploreExperienceActivity1](https://s3-eu-west-1.amazonaws.com/pachatary/static/explore-1-android-screenshot.png)
![ExploreExperienceActivity2](https://s3-eu-west-1.amazonaws.com/pachatary/static/explore-2-android-screenshot.png)

This is the main tab.
By default, it asks location permissions
and shows a search without word, just by proximity and popularity.
User can scroll viewing a summary of each experience.
Clicking over an experience navigates to experience detail.
Clicking over a profile picture or username navigates to profile.
Search can be customized with text filling the top search bar.
Location is also editable clicking map icon and selecting a location.

##### SavedExperiencesTab

![SavedExperienceActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/saved-android-screenshot.png)

This screen shows saved experiences.
Clicking an experience navigates to experience detail.

##### MyExperiencesTab

![MyExperiencesActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/myexperiences-android-screenshot.png)

This is the self profile activity.
Here user can view and edit her profile.
A click on profile picture allows to change it.
Tapping the bio edit text allows to edit it.
Share icon show a dialog to share self profile url.
Settings icon navigates to settings.
On the bottom of the screen, self experiences appear on a scroll.
Finally, green + button navigates to create a new experience.

When person is not completely registered,
this screen redirects to register.


#### RegisterActivity

![RegisterActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/register-android-screenshot.png)

An anonymous will be able to post content after register and email confirmation.
In this form the user inputs her email and username
to register and receive that confirmation email.


#### ProfileActivity

![ProfileActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/profile-android-screenshot.png)

User navigates to this screen clicking on a profile picture or username.
Same screen as MyExperiences but just showing person's profile.


#### ExperienceScenesActivity

![ExperienceScenesActivity1](https://s3-eu-west-1.amazonaws.com/pachatary/static/experience-detail-1-android-screenshot.png)
![ExperienceScenesActivity2](https://s3-eu-west-1.amazonaws.com/pachatary/static/experience-detail-2-android-screenshot.png)
![ExperienceScenesActivity3](https://s3-eu-west-1.amazonaws.com/pachatary/static/experience-detail-3-android-screenshot.png)

This is the detail of an experience.
Here appear the experience and its scenes information.
Experience can be shared or saved with the top buttons.
Clicking profile picture or username navigates to profile screen.
Clicking on the map, it shows the scenes located over a map.
Clicking scene navigation button also navigates to the map, but locating the selected scene.
Descriptions are trunkated and can be expanded clicking the show more text.
If the experience is mine, save button is changed by edit experience and add scene buttons.
Also, if the user can edit the experience,
an edit button appears on the top right corner of each scene.


#### ExperiencesMapActivity

![ExperiencesMapActivity1](https://s3-eu-west-1.amazonaws.com/pachatary/static/experience-map-android-screenshot.png)

This screen shows the scenes from the selected experience
(navigation toolbar shows its title)
over a visual map, provided by [Mapbox](https://www.mapbox.com/).
It places a marker over the exact position of each scene
and when it is clicked shows a bubble with the scene title.
When a bubble is tapped, app navigates to show its scene detail.


#### CreateExperienceActivity & EditExperienceActivity

![CreateExperienceActivity1](https://s3-eu-west-1.amazonaws.com/pachatary/static/create-experience-android-screenshot.png)
![EditExperienceActivity2](https://s3-eu-west-1.amazonaws.com/pachatary/static/edit-experience-android-screenshot.png)

This screens allows you to both create a new or edit old experience.
Add image icon navigates to pick and crop image.

#### CreateSceneActivity & EditSceneActivity

![CreateSceneActivity1](https://s3-eu-west-1.amazonaws.com/pachatary/static/create-scene-android-screenshot.png)
![EditSceneActivity2](https://s3-eu-west-1.amazonaws.com/pachatary/static/edit-scene-android-screenshot.png)

This screens allows you to both create a new or edit old scene.
Same as experience's ones but with location selection.


#### SelectLocationActivity

![SelectLocationActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/select-location-android-screenshot.png)

This screen appear when you edit scene location,
but also when you choose search location on explore screen.
It allows the user to move the map to point an specific location.
The search bar is used to search an specific address, city or country.
Locate icon centers the map on the current user location.


#### PickAndCropImageActivity

It uses [Matisse](https://github.com/zhihu/Matisse) library to make
user pick an image from gallery for old versions.
Default documents app is used on new devices.
It uses [uCrop](https://github.com/Yalantis/uCrop) library to let
user crop image to make it square.
It also handles storage permissions.


#### SettingsActivity

![SettingsActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/settings-android-screenshot.png)

Settings screen, to navigate to terms and conditions, privacy policy or send a feedback email.


#### TermsAndCondictionsActivity

![TermsAndConditionsActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/terms-android-screenshot.png)

Screen that shows terms and conditions.
Privacy policy screen is the same as this.


## Documentation

The architecture of this app has 2 main characteristics:

* Code structure follows the **Clean Architecture** approach
with a little modification:
Use cases are not implemented unless required.
Correct responsibility assignment and deep unit testing are main goals.
Views are totally passive
and there are no visual tests yet.

* The app is totally **Reactive**, and repos cache most of the information that fetch from server.
RxJava plays a very important role on the system.
A normal view only has to subscribe what it needs and react to events received.
Repositories are split in frontal and api.
The second one manages the retrofit requests,
and the first one handles caching,
both made reactively to reach the subscribed presenters.

Frontal repository uses an special pattern that consists on
merging the different source of item modification flowables
and making them emit functions instead of objects.
That allows streams to modify the state of the cached
elements using `scan` RxJava operator.

Paginated caches are wrapped by a requester,
that handles every request from the view with the internal cache explained above.

All classes follow dependency injection pattern (with Dagger2).

100% Kotlin!

### Authentication

When the app starts, checks if the user has credentials stored locally.
If not, it calls the api to create a guest person instance and get credentials.
To create content, person needs to be also registered and confirm the email.
There is a RegisterActivity and ConfirmEmailActivity that does that and
saves person info locally.

Login is the other way that a person can use to authenticate herself.

All api calls are authenticated using `HttpAuthInterceptor`.

## Setup

Follow these instructions to start working locally on the project:

First of all, you must run api server locally.
Code and setup instructions can be found in this other repo:
[pachatary-api](https://github.com/jordifierro/pachatary-api).
You also have to register at [Mapbox](https://www.mapbox.com/)
to get a token to use their api.
Finally, create a project at [Firebase](https://firebase.google.com/)
and create both debug and relase projects and get their `google-services.json`.

Once server is working, we must:

* Download code cloning this repo:
```bash
git clone https://github.com/jordifierro/pachatary-android.git
```
* Copy `app.properties.sample` file to `app.properties`
and fill the fields following the hint instructions:
```bash
cp app/app.properties.sample app/app.properties
```
* Create a keystore with Android Studio to fill last properties:
`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`,
`RELEASE_KEY_ALIAS` and `RELEASE_KEY_PASSWORD`

* Move `google-services.json` files to release and debug:
```bash
mv release-google-services.json app/
mkdir app/src/debug
mv debug-google-services.json app/src/debug
```
* Run tests:
```bash
./gradlew test
```

* You are ready to run the application on your device!
