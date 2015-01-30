use(function () {
    var test = {};

    test.text = properties.get('text') ||  resource.getPath();
    test.tag = properties.get('tag') || null;
    if (test.tag != null) {
        test.startTag = '<' + test.tag + '>';
        test.endTag = '</' + test.tag + '>';
    }
    test.includeChildren = properties.get('includeChildren') || false;
    if (test.includeChildren) {
        var sly = (typeof sightly === "undefined") ? granite : sightly;
        test.children = sly.resource.getChildren();
    }

    return test;
});