The repository is a solution to a take-home assignment, that a candidate for Android Software Engineer role at one of the Swiss financial instition was asked to complete during the interviewign process.
The exact assignment statement may be found at the bottom of the page.


### Original Assignment

```
Introduction

Our client is the travel agency, which is looking for the diverse ways to increase the growth of the their core business: booking of the flights and hotels for the private customers.
Now they aim to help people in long-distance relationships spend time together. For this, they have an idea to show couples when they can meet on public holidays.
Like any Proof of Value project, this travel agency wishes to first test if people are interested in such service before investing in the further development.
They have an idea to add an abbility of  booking hotels and flights for such holiday dates at the later stages. This way in the long run they are planing to increase user base of their core business.
An assignment is to implement a PoV of the mobile app.
You are the lead developer and the architect of this project – feel free to take any technical decisions on your own.

---

Assignment

1. App should take two countries as an input: country A and country B
2. Outputs of the app should be:
   - The list of public holidays common to both country A and country B
   - The list of holidays that are only in country A, but not in country B
   - The list of holidays that are only in country B, but not country A
   - Bonus feature: collapse contiguous holidays, e.g., if April 1 and 2 are both holidays, report it as a single holiday
3. Define if/how app should handles unexpected situations, e.g., no internet connection.
4. Right now Hilt is used as DI, coroutines are used for Asynchronous operations, java.time is used for date/time, Moshi is used for serialization and Compose is used as UI toolkit in all existing client’s apps. Since devs are familiar with this stack it would be preferable solution for the new app as well. However, if you have strong reason to use other libs/tools – you can use it as well.

Also, some more hints from our side for the implementation phase:
- You can use https://holidayapi.com to find out holidays in a country. The results can be limited to the current year only.
- This is an MVP, which means that the app will be used for user testing.
- On the one hand, this means you do not need to build the most scalable app, ready to go on the Store.
- On the other hand, the prototype will be changed according to customer feedback, and the changes should happen fast.
- You should prefer using modern tech stack if possible
- The UI / UX design is left to your choice.

Feel free to start with implementation and create and share a new private repo in the Github/Bitbucket.
Before starting the implementation, please commit the Readme in the same repo where app source code will be hosted. Feel free to add any important details about app architecture, app screens and suggested frameworks/libs. High level estimates can be mentioned here as well.
The format is up to you - e.g. simple markdown document can be used.
```
