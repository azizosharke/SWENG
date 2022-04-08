import json
from typing import TextIO

from flask import Flask, Response
from flask_cors import CORS, cross_origin
from flask import request
import uuid
from multiprocessing import Process, Lock
import jsonpickle

dictLock = Lock()
app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'


class SearchQuery:
    def __init__(self, file_name_to_search, text_to_search):
        self.file_name_to_search = file_name_to_search
        self.text_to_search = text_to_search
        self.status = "in_progress"
        self.found_matches = 0

    def reset(self):
        self.status = "in_progress"
        self.found_matches = 0


# the functions load_ids_search_query_dict and save_ids_search_query_dict are used for loading and saving
# dictionaries to and from text files this is the only way of different processes in the server to share the same
# dictionary (as far as I know)

# loads dictionary from text file
def load_ids_search_query_dict() -> 'dict[str, SearchQuery]':
    dictLock.acquire()
    f: TextIO = open("ids_search_query_dict.txt", 'r')
    dict_string = f.read()
    try:
        result: dict[str, SearchQuery] = jsonpickle.decode(dict_string)
    except Exception:
        result: dict = {}
    f.close()
    dictLock.release()
    return result


# resets a SearchQuery object to "in_progress" and found_matches = 0 in ids_search_query_dict
def reset_search_query_in_sq_dict(search_query: SearchQuery, sq_dict: 'dict[str, SearchQuery]') -> None:
    search_query.reset()
    save_ids_search_query_dict(sq_dict)


# saves dictionary to text file
def save_ids_search_query_dict(dictionary: 'dict[str, SearchQuery]') -> None:
    dictLock.acquire()
    f: TextIO = open("ids_search_query_dict.txt", 'w')
    dict_string: str = jsonpickle.encode(dictionary)
    f.write(dict_string)
    f.close()
    dictLock.release()


# SEARCHES A GIVEN FILE FOR A STRING OF TEXT PASSED
# Will count words containing a specified string
# EX. If text is 'apple' will count pine'apple'
def text_search(file_path, text, unique_id):
    count = 0
    text_capital = None
    text_lowercase = None
    lowercase = False
    uppercase = False

    # Check if text starts lowercase
    if text[0].islower():
        text_capital = text.capitalize()
        uppercase = True
    # Check if text starts uppercase
    if text[0].isupper():
        text_lowercase = text.lower()
        lowercase = True

    f = open(file_path, 'r')
    text_string = f.read()
    if text in text_string:
        count += text_string.count(text)
        # Check for text starting capitalized
        if uppercase is True:
            count += text_string.count(text_capital)
        # Check for text starting non-capitalized
        if lowercase is True:
            count += text_string.count(text_lowercase)
    f.close()

    ids_search_query_dict = load_ids_search_query_dict()
    ids_search_query_dict[unique_id].status = "done"
    ids_search_query_dict[unique_id].found_matches = count
    save_ids_search_query_dict(ids_search_query_dict)

    return count


# server function to handle the GET request
@app.route("/search/<unique_id>", methods=["GET"])
@cross_origin()
def index(unique_id):
    ids_search_query_dict = load_ids_search_query_dict()
    if unique_id not in ids_search_query_dict.keys():
        return Response(json.dumps({"status": "error_id_not_found", "found_matches": 0}))
    else:
        return Response(json.dumps({"status": ids_search_query_dict[unique_id].status,
                                    "found_matches": ids_search_query_dict[unique_id].found_matches}))


# server function to handle the POST request
@app.route('/search', methods=["POST"])
def post_request():
    request_data: dict[str, str] = json.loads(request.data)
    file_name_to_search: str = request_data["search_file"]
    text_to_search: str = request_data["search_text"]

    # checks if corresponding pair of file_name, and text_to_search already exits in our ids_search_query_dict.txt
    ids_search_query_dict: dict[str, SearchQuery] = load_ids_search_query_dict()

    for key, value in ids_search_query_dict.items():
        if value.file_name_to_search == file_name_to_search and value.text_to_search == text_to_search:
            reset_search_query_in_sq_dict(value, ids_search_query_dict)
            # return existing ID if it already exists
            p = Process(target=text_search, args=(
                file_name_to_search, text_to_search, key))
            p.start()
            return Response(json.dumps({"search_id": key}), status=202)

    # else
    # if search query does not exist, make a new search query and add it to the dict
    unique_id = str(uuid.uuid4())
    ids_search_query_dict[unique_id] = SearchQuery(
        file_name_to_search, text_to_search)
    save_ids_search_query_dict(ids_search_query_dict)

    # starts the search process
    p = Process(target=text_search, args=(
        file_name_to_search, text_to_search, unique_id))
    p.start()

    return Response(json.dumps({"search_id": unique_id}), status=202)


@app.errorhandler(404)
@cross_origin()
def page_not_found(e):
    return "NotFoundError"


# starts the server
if __name__ == "__main__":
    open("ids_search_query_dict.txt", 'w').close()  # clears dictionary file
    app.run(host="127.0.0.1", debug=False)
