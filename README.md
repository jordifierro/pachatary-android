# Pachatary Android App
This repo contains the Android application
code for Pachatary project.
This simple app aims to be a reference
to discover interesting places
and also to record our past exploreExperiences,
sharing content with other people.

Application domain is composed by `scenes`,
which are defined as something that happened
or can be done/seen in a located place.
A group of `scenes` are defined as an `experience`.

## App screens

Application is (and will be) under construction.
There are already some screens.
Here is a quick documentation about them:

#### MainActivity
![ExperienceListActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/main-activity-screenshot.jpg)

This is the screen that appears when you open the app.
There are three section tabs: mine, saved and explore.
All of them show experiences (title and picture) in a scrollable list.
When an item is clicked,
it navigates to the selected experience map view.


#### ExperienceMapActivity
![ExperienceMapActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/experience-map-screenshot.jpg)

This screen shows the scenes from the selected experience
(navigation toolbar shows its title)
over a visual map, provided by [Mapbox](https://www.mapbox.com/).
It places a marker over the exact position of each scene
and when it is clicked shows a bubble with the scene title.
When a bubble is tapped, app navigates to show its scene detail.
There is a button to save or unsaved the experience.
If experience is mine, edit button appears instead of save one.

#### SceneListActivity
![SceneListActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/scene-list-screenshot.jpg)

Finally, this screen shows the selected scene detail:
picture, title and description.
Scroll allows user to view other scene details and also experience detail on top.

#### Create(Scene/Experience)Activity and Edit(Scene/Experience)Activity

This screens are used to handle the scene and experience creation and edition form flow
(that are almost the same).
They only appears visible to the user to show a loader while creating the scene/experience
or ask user if wants to modify the scene/experience picture.
Here are the form screens:

##### EditTitleAndDescriptionActivity
![EditTitleAndDescriptionActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/edit-title-and-description-screenshot.jpg)

This screen simply lets the user introduce a title and description.
Title has to be between 1 and 30 chars.

##### SelectLocationActivity
![SelectLocationActivity](https://s3-eu-west-1.amazonaws.com/pachatary/static/select-location-screenshot.jpg)

This activity shows a map to allow user select a location (only for scenes).
It starts with the last known user location to make the process faster.

##### PickImageActivity
![PickImageActivity](https://raw.githubusercontent.com/zhihu/Matisse/master/image/screenshot_zhihu.png)

It uses [Matisse](https://github.com/zhihu/Matisse) library to make
user pick an image from gallery.

#### CropImageActivity
![CropImageActivity](https://lh3.googleusercontent.com/1AaLxbfgADNljb4626mYYydeVSrpe1rGX04v25SRbDMPc2yO0O0fpLY2Wxz4TRva4Q=h900)

It uses [uCrop](https://github.com/Yalantis/uCrop) library to let
user crop image to make it square.


## Documentation

Correct responsibility assignment and deep unit testing are main goals.
Code structure follows the Clean Architecture approach
with a little modification:
Use cases are not implemented unless required.

Views are totally passive
and there are no visual tests yet.

RxJava plays a very important role on the system.
Repositories are split in frontal and api.
The second one manages the retrofit requests,
and the first one handles caching,
both made reactively to reach the subscribed presenters.

Frontal repository uses an special pattern that consists on
merging the different source of scenes modification flowables
and making them emit functions instead of scene objects.
That allows streams to modify the state of the cached
elements using `scan` RxJava operator.

All classes follow dependency injection pattern (with Dagger2).

100% Kotlin!

### Authentication

When the app starts, checks if the user has credentials stored locally.
If not, it calls the api to create a guest person instance and get credentials.
To create content, person needs to be also registered and confirm the email.
There is a RegisterActivity and ConfirmEmailActivity that does that and
saves person info locally.
All api calls are authenticated using `HttpAuthInterceptor`.

## Setup

Follow these instructions to start working locally on the project:

First of all, you must run api server locally.
Code and setup instructions can be found in this other repo:
[pachatary-api](https://github.com/jordifierro/pachatary-api).

Once server is working, we must:

* Download code cloning this repo:
```bash
git clone https://github.com/jordifierro/pachatary-android.git
```
* Copy `app.properties.sample` file to `app.properties`:
```bash
cp app/app.properties.sample app/app.properties
```
* Run tests:
```bash
./gradlew test
```
* Fill `app.properties`: `apiUrl` with your local server url
and get a [Mapbox](https://www.mapbox.com/) account
to get your own `mapboxAccessToken`,
`clientSecretKey` must be agreed with server and also set in properties (same for `dev-`).

* You are ready to run the application on your device!
