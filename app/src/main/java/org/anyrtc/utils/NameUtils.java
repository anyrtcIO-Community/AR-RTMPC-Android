package org.anyrtc.utils;

import java.util.Random;

/**
 * Created by Skyline on 2016/8/8.
 */
public class NameUtils {
    public static String[] userNickname = new String[]{"John", "Michelle", "Amy", "Kim", "Mary", "David", "Sunny", "James", "Maria", "Michael", "Sarah", "Robert", "Lily"
            , "William", "Jessica", "Paul", "Crystal", "Peter", "Jennifer", "George", "Rachel", "Thomas", "Lisa", "Daniel", "Elizabeth"
            , "Kevin", "Angela", "Richard", "Emily", "Charles", "Eva", "Jason", "Jenny", "Mark", "Alice", "Eric", "Candy", "Chris", "Linda"
            , "Jack", "Tina", "Alex", "Sara", "Edward", "Emma", "Tony", "Anne", "Joseph", "Cindy", "Henry", "Grace", "Alan", "Susan", "Anna"
            , "Maggie", "Christian", "Annie", "Tom", "Rebecca", "Andy", "Claire", "Carlos", "Vanessa", "Steven", "Judy", "Stephen", "Catherine"
            , "Jean", "Helen", "Andrew", "Christina", "Jonathan", "Karen", "Frank", "Marco", "Elaine", "Gary", "Nana", "Antonio", "Nicole"
            , "Alexander", "Margaret", "Matthew", "Julia", "Louis", "Lucy", "Jose", "Natalie", "Martin", "Kate", "Patrick", "Olivia", "Sam"
            , "Betty", "Angel", "Laura", "Anthony", "Ellen", "Luis", "Elena", "Leo", "Samuel", "Vicky", "Brian", "Summer", "Sean", "Nancy"
            , "Carl", "Zoe", "Diego", "Teresa", "Danny", "Wendy", "Jerry", "Christine", "Karl", "Princess", "Nick", "Barbara", "Albert"
            , "Julie", "Ryan", "Amber", "Johnny", "Stephanie", "Mike", "Sharon", "Aaron", "Sophia", "Kelly", "Yvonne", "Tim", "Tiffany"
            , "Roberto", "Marie", "Alfred", "Simon", "Lauren", "Andrea", "Gina", "Diana", "Mario", "Doris", "Victor", "Fiona", "Ivan"
            , "Victoria", "Fernando", "Caroline", "Adam", "Ivy", "Miguel", "Alexandra", "Raymond", "Jessie", "Ray", "Lulu", "Arthur"
            , "Janet", "Ricardo", "King", "Cynthia", "Vincent", "Cherry", "Jeremy", "Bonnie", "Andre", "Isabella", "Christopher", "Kitty"
            , "Dennis", "Mia", "Harry", "Rose", "Leon", "Cecilia", "Jacky", "Louise", "Bob", "Rita", "Bill", "Katie", "Sebastian", "Erica"
            , "Oscar", "Jacqueline", "Philip", "Ruby", "Benjamin", "Miranda", "Jim", "Iris", "Jeff", "Queen", "Scott", "Esther", "Kenneth"
            , "Melissa", "Justin", "Nathan"};

    public static String getNickName() {
        return userNickname[new Random().nextInt(201)];
    }
}
