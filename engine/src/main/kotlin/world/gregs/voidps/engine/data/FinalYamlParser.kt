package world.gregs.voidps.engine.data

class FinalYamlParser {

    var input = ""
    var index = 0

    /*

    TODO impl the single lines and helper functions first as they can be tested stand alone


        parse root
            given a start point
            peek first char after spaces
            if first char is '-'
                parse list
            if first char is #
                parse comment
                parse root
            else
                parse map

        parse list
            create list
            while true
                given a start point
                peek first char after spaces
                if first char is #
                    parse comment
                else
                    count number of spaces
                    calculate indent
                    if indent equals previous indent
                        parse value
                        add parsed value
                    if indent greater than previous
                        error - expected block end but found start
                    else
                        break
                save previous indent

        parse comment
            consume until end of line
            consume \n

        parse value
            given a start point
            peek first char after spaces
            if first char is '-'
                consume '-'
                consume excess space
                return parse type
            else if first char is '#'
                parse comment
                return null
            else if peek value has :
                return parse map
            else
                return parse type

        parse type (with limit)
            if char is false
                return false
            else if char is true
                return true
            else if char first is [
                return parse line list
            else if char first is {
                return parse line map
            else if char first is "
                return parse quoted string
            else
                while index isn't ':' or \n or greater than limit
                    increase index
                if char is \n
                    consume \n
                return string

        parse map
            given a start point and indent
            create map
            while true
                count current indent
                if current indent equals indent
                    parse key-value pair
                    set map key-value pair
                else if indent greater than previous
                    parse value
                    set map parsed value
                else
                    error - bad format expected block end
            return map

        parse key-value pair
            given previous indent
            parse key
            if next char is \n
                consume \n
                peek indent
                peek char after spaces
                if indent equals previous indent
                    return key and null as map
                if indent greater than previous
                    parse value
                    return parsed value
            else
                parse type
                return key and parsed type as map

        parse key
            skip excess spaces
            mark start point
            while value isn't ':'
                increase index
            key is string from start point to index
            consume ':'
            consume any excess space
            return key

        parse line list
            create list
            var for open and closed []'s
            consume excess spaces
            consume [
            consume excess spaces
            temp index
            while count is zero and char isn't ] and less than limit
                if char is [
                    increase count
                if char is ]
                    decrease count
                if char is ,
                    parse type with limit of temp index
                    add parse type
                increase temp index
                set last index to temp index
            consume ]
            consume excess spaces
            return list

        parse line map
            create map
            var for open and closed {}'s
            consume excess spaces
            consume {
            consume excess spaces
            temp index
            while count is zero and char isn't } and less than limit
                if char is }
                    increase count
                if char is }
                    decrease count
                if char is ,
                    parse key
                    parse type with limit of temp index
                    set key to parse type
                increase temp index
                set last index to temp index
            consume }
            consume excess spaces
            return map


        "- apple\n- banana\n- orange"
        "# list of fruits\n- apple\n     # my favourite\n- banana \n- orange # not the colour"
        "- \"apple\"\n- \"banana\"\n- \"orange\"\n  - \"pear\""

     */
    fun parse(text: String): Any {
        input = text
        index = 0
        return emptyList<Any>()
    }
}