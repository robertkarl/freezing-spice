CodePath SF Week 2 - Robert Karl - Google Image Search
=============
Download [RK Image Search](https://play.google.com/store/apps/details?id=net.robertkarl.gridimagesearch.app) on the Google Play Store.

#### Time
~25 hours


#### User Stories
- [x] User can enter a search query that will display a grid of image results from the Google Image API.
- [x] User can click on "settings" which allows selection of advanced search options to filter results
- [x] User can configure advanced search filters such as size, color, type, and site
- [x] Subsequent searches will have any filters applied to the search results
- [x] User can tap on any image in results to see the image full-screen
- [x] User can scroll down “infinitely” to continue loading more image results (up to 8 pages)

Optionals

- [x] Error handling: display an error screen on the results page. Retry with exponential backoff and disable the error state when connection returns.
- [x] Use the ActionBar SearchView as the query box (added this to the side nav)
- [x] User can share an image to their friends or email it to themselves
- [ ] Replace Filter Settings Activity with a lightweight modal overlay
- [x]  Improve the user interface and experiment with image assets and/or styling and coloring (some lightweight styling done)
- [ ] Use the StaggeredGridView. Implemented this but did not get good results. Poor scroll performance with SmartImageView.
- [x]  User can zoom or pan images displayed in full-screen detail view

Extra
- [x] Add side navigation with history. Allow editing history.


#### GIF Walkthrough

![Walkthrough](https://raw.githubusercontent.com/robertkarl/freezing-spice/master/ImageSearchDemo.gif)

#### Test Plan
- Run a search with internet disabled. Error state should be displayed.
- Re-enable internet.
- Error state should disappear.

- Run a search that returns zero results, like "asdfasdfqqqqewrt"
- Empty state view should appear.
- Run a search that returns results. _expected result_ results should appear and empty state disappears.
