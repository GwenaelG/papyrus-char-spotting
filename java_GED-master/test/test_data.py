import time
import sys
import os

def make_cxl(graph_filename_list, cxl_filename, p_working_dir):

    file_name = cxl_filename
    valid_file_name = os.path.join(p_working_dir,  file_name)
    header_lines = ['<?xml version="1.0"?>\n', '<GraphCollection>\n','<graphs>\n' ]
    footer_lines = ['</graphs>\n', '</GraphCollection>\n']

    file_h = open(valid_file_name, 'w')
    file_h.writelines(header_lines)
    for graph_filename in graph_filename_list:
        file_item_str  = '<print file="{0}" class="A"/>\n'.format(graph_filename)
        file_h.write(file_item_str)
    file_h.writelines(footer_lines)
    file_h.close()


def make_prop_file(cxl_train_list, cxl_target_list, prop_file_name, p_working_dir, p_dir_path, p_alg_type):

    for target_item in cxl_target_list:
        for source_item in cxl_train_list:

            (cn, ce, alpha, band_value, hed_context) = (32, 32, 0.5, 0, 0)

            prop_full_path = os.path.join(p_working_dir, prop_file_name)
            prop_h = open(prop_full_path, 'w')

            prop_h.write("# graph edit distance method\n")
            prop_h.write('matching={0}\n'.format(p_alg_type))
            
            prop_h.write('s=\n')
            prop_h.write('\n')
            prop_h.write('# paths\n')
            prop_h.write('source={0}\n'.format(p_working_dir + '/' + source_item))
            prop_h.write('target={0}\n'.format(p_working_dir + '/' + target_item))
            prop_h.write('path={0}\n'.format(p_dir_path+ '/') )
            prop_h.write('result={0}/\n'.format(p_working_dir + '/' + 'result'))
            prop_h.write('\n')
            prop_h.write('# parameters\n')
            prop_h.write('node={0}\n'.format(cn) )
            prop_h.write('edge={0}\n'.format(ce) )
            prop_h.write('alpha={0}\n'.format(alpha) )
            prop_h.write('contextRadius={0}\n'.format(hed_context))
            prop_h.write('band={0}\n'.format(band_value) )
            prop_h.write('\n')

            file_footer_h = open('costskws.txt','r')
            str_footer = file_footer_h.read()
            file_footer_h.close()
            prop_h.write(str_footer)
            prop_h.close()


def test_one_by_one(p_dir_path, p_working_dir):
    '''
    This function tests the data one by one
    not reaally necessary now
    :return:
    '''

    gxl_list = [file_name for file_name in os.listdir(p_dir_path) if '.gxl' in file_name]
    time_in = time.clock()
    for file1 in gxl_list:
        make_cxl([file1], source_cxl, p_working_dir)
        for file2 in gxl_list:
            make_cxl([file2], target_cxl, p_working_dir)
            os.system(cmd_txt)
        break
    time_out = time.clock()
    print 'time spend:', (time_out-time_in)


def test_case(p_dir_path, p_working_dir):
        gxl_list = [file_name for file_name in os.listdir(p_dir_path) if '.gxl' in file_name]
        if len(gxl_list) == 0:
            print 'Nothing in ', p_dir_path
            return
        time_in = time.clock()
        make_cxl(gxl_list, source_cxl, p_working_dir)
        make_cxl(gxl_list, target_cxl, p_working_dir)
        os.system(cmd_txt)
        time_out = time.clock()
        print 'time spend:', (time_out-time_in)


if __name__ == '__main__':
    argv = sys.argv[1:]

    java_GED_str = '../out/artifacts/java_GED_jar/java_GED.jar'
    source_cxl = 'source_cxl_test.txt'
    target_cxl = 'target_cxl_test.txt'
    prop_file_name = 'prop_test.txt'

    if len(argv) == 0:
        print 'call python test_data.py test_data'
        exit()

    working_dir = argv[0]
    cmd_txt = "java -Xms1024m -Xmx2048m -jar {1} {0} ".\
                    format('{0}\\{1}'.format(working_dir, prop_file_name), java_GED_str)

    #prop_h.write('matching={0}\n'.format('HEDCotext'))
    #prop_h.write('matching={0}\n'.format('HEDOneMatch'))
    #prop_h.write('matching={0}\n'.format('HEDSelfCost'))

    for dir_item in os.listdir(working_dir):
        dir_path = working_dir +'/'+  dir_item
        if not os.path.isdir(dir_path):
            continue
        print 'testing data in', dir_path
        make_prop_file([source_cxl], [target_cxl], prop_file_name, working_dir, dir_path, 'HED')
        test_case(dir_path, working_dir)

