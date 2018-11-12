/**
 * 处理压缩源码,分割成每个单独的文件
 * 源码格式如:
 * {
 *  './locales.json':{something}
 *  './src/app/index.js':(function(){})
 * }
 */
const fs = require('fs');
const path = require('path');

/**
 * 同步递归创建路径
 *
 * @param  {string} dir   处理的路径
 * @param  {function} cb  回调函数
 */
let $$mkdir = function (dir, cb) {
    let pathinfo = path.parse(dir);
    if (!fs.existsSync(pathinfo.dir)) {
        $$mkdir(pathinfo.dir, function () {
            fs.mkdirSync(pathinfo.dir)
        })
    }
    if (cb) {
        cb()
    } else {
        if (!fs.existsSync(dir))
            fs.mkdirSync(dir)
    }
};

function fn(name, value) {
    $$mkdir(path.join(__dirname, path.dirname(name)));
    fs.writeFileSync(name, value)
}

fs.readFile('./app.js', 'utf-8', function (err, data) {
    if (err)
        throw err;
    let obj = eval('(' + data + ')');
    let n = 0;
    for (let item of Object.keys(obj)) {
        fn(item, obj[ item ]);
        console.log(`第${n++}次写入${item}`)
    }
});


