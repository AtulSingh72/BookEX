var passport = require("passport");
var GoogleStrategy = require("passport-google-oauth20");
var User = require("../models/User");

passport.serializeUser(function (User, done) {
    done(null, User);
});

passport.deserializeUser(function (User, done) {
    done(null, User);
});

passport.use(
    new GoogleStrategy(
        {
            // Goorm
            // clientID: "1040941249609-oscm85g83ueshgs930pvncpsdmdcif6e.apps.googleusercontent.com",
            // clientSecret: "bcNyHqPiFtsXUpTDUwme213T",
            // callbackURL: "https://kitab-buddy.run-us-west2.goorm.io/signup/google/callback"
            // Heroku
            callbackURL:
                "http://book-exchnge.herokuapp.com/signup/google/callback",
            clientID:
                "921117793019-6r4on28a2c1j8a6tf95ogmp82cpqi7jj.apps.googleusercontent.com",
            clientSecret: "M3uhkGt4D8RcBQNPUQ0vIROf",
            // Localhost
            // callbackURL: "http://localhost:8080/signup/google/callback",
            // clientID:
            //     "421566432651-p7ta0j2b67os8liqrvku3vt4ejf5i5lp.apps.googleusercontent.com",
            // clientSecret: "YXi_Xo-uF4pNVV_PZ3aBZkDr",
        },
        async function (accessToken, refreshToken, profile, done) {
            console.log(refreshToken);
            await User.findOneAndUpdate(
                { username: profile._json.email },
                {
                    accessToken: accessToken,
                    refreshToken: refreshToken,
                    username: profile._json.email,
                    firstname: profile._json.given_name,
                    lastname: profile._json.family_name,
                    userid: profile.id,
                },
                { upsert: true, new: true, setDefaultsOnInsert: true },
                function (err, user) {
                    return done(err, user);
                }
            );
        }
    )
);

module.exports = passport;
