for /r %%i in (*) do java -jar MJCompiler.jar -input %%i -output %%i.obj -dump_disassembly >%%i.out 2>%%i.err