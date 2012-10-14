GH-Pages
========

I think that there must be an easier way to go about this, my way seems to be somewhat circuitous. Anway, it has been working so far, although I still get some non-fast-forward errors when I attempt to push changes. I haven't been able to figure out why these came up, it still pushed the changed even while giving me an error telling me that it couldn't do it. 

###The process that I have been using to update gh-pages:###

1. Make changes in Skynet
2. Fetch updated html from docbuilder server
3. Replace the images section with my formatted version
4. Remove/replace chapter 5 and appendix with formatted appendix 
5. Commit and push

If you have a look through the html for index.html before updating it, the two sections that I have formatted are pretty blatent. Those were the only bits that I wanted to be able to check on/edit a bit, so I didn't bother with the rest. I couldn't find an html formatter that I was happy with for gedit or emacs, so I would definitely recommend using an IDE for editing that will let you click the format button. I didn't want to download an IDE when all I wanted was some html editing, so I just formatted by hand. 

###Adding images:###

1. Add in the usual fashion to Skynet
2. Fetch updated html from docbuilder server
3. Add new image files to images/ 
4. Rename files to the designation provided by Skynet
5. Commit and push

###Updating/Changing images:###

1. Remove locale from image in Skynet
2. Add new locale with new image in Skynet
3. Remove old image file from images/
4. Add new image files to images/ 
5. Rename files to the designation provided by Skynet
6. Commit and push
