# YBS-Mobile-Challenge
Native Android app that uses the Flickr API to display a list of photos.

Brief
Develop a native mobile app which uses the Flickr API to display a list of photos.
The look and feel of the app is up to you, but it should adhere to normal expectations for a user of that platform. The app should display a list of photos, along with the poster's userid and user icon. The app should display alongside the photo a list of any tags associated with the photo. Tapping on the photo should take the user to a separate page wherein they can see more detail about the photo (e.g. Title, date take,  any content description, etc.), Tapping on a user id/photo should produce a list of photos by that user. There should be a default search of "Yorkshire" on first load and safe_search must be set to safe. 

Decisions made: 

- The app is made entirely with Kotlin and XML. These are what I'm currently most comfortable in and have the most experience with so given the time constraints I stuck with what I know.
- Using fragments. Most of my projects in the past have used fragments. I find it's the best way for future development and potential customisability between different device sizes and screen real estate.
- Glide and OKHTTP3 libraries. I have used both of these libraries in the past so I know their capabilities and particularly with Glide I know it's able to perform the tasks I needed from them with ease and reliably. 

Over all thoughts:
So I was a bit pressed for time on this as I haven't had much free time this week so I did have to make some time saving decisions that if I had a freer week I would've done. For example jetpack compose. 
As you might be able to tell from the commits I did start the app with the intention to include a mix of jetpack compose and XML files. 
Unfortunately as the week dragged on and I still hadn't been able to sit down and get going I decided it would be better to get the main brief out in full in the timeframe and my best hopes of doing that was in XML where I have had more experience. 

