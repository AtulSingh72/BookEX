var mongoose = require("mongoose");
const eBook = require("./eBook");

var ReportSchema = new mongoose.Schema({
    object: {
        type: mongoose.Schema.Types.ObjectId,
        refPath: "object_model",
    },
    object_model: {
        type: String,
        enum: ["Book", "Misc", "EBook"],
    },
    reporter: { type: mongoose.Schema.Types.ObjectId, ref: "User" },
    comment: String,
});

ReportSchema.statics.findOrCreate = require("find-or-create");
module.exports = mongoose.model("Report", ReportSchema);
