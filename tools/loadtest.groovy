socket = new Socket("localhost", 31337)
(0..1000).each {
    str1 = UUID.randomUUID().toString() << ";PURCHASE;" << new Date().getTime() << ";0;5942,5981,5950"
    str2 = UUID.randomUUID().toString() << ";REFUND;" << new Date().getTime() << ";0;5942,5981,5950"
    println str1
    socket << str1 << "\n"
    println str2
    socket << str2 << "\n"
}
