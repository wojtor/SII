SII
==================

## CMR is the Contextual Music Recommendation Project
- CMR was realized by using weka, jsoup libraries, eco nest api(close to Spotify), java 
The classifier in this case was NaiveBayesMultinomialUpdateable and the filter used was StringToWordVector
- The training set was made by pseudo-docs there were build thanks to eco nest api.
So for every music artist there were retrieved informations like Last News, Some Songs and Biography.
- The classifier is classifying the content of given URL by similarity between attributes.
- As the result there are show 2 most similar artist to the content and for each artist are retrieved also 5 more similar artist of the result.

## src folder contains java code:

* ContextualMusicRecommendation
* DataRaw
* MusicClassifier
* SiteWrapper


## exec.rar contains:

Just download the exec.rar and try it running the .exe in same unpack folder

* CMR jar file
* artists.txt
* data.txt
* ContextMusicRecom.exe