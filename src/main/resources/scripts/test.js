function test(context, message) {
    var keys = message.payload();
    Log.error(keys);
    for (k in keys) {
        Log.error(keys[k]);
    }
    Log.debug("debug");

    return 'Done';
}